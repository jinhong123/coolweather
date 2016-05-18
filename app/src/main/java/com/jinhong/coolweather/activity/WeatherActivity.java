package com.jinhong.coolweather.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jinhong.coolweather.R;
import com.jinhong.coolweather.service.AutoUpdateService;
import com.jinhong.coolweather.util.HttpCallbackListener;
import com.jinhong.coolweather.util.HttpUtil;
import com.jinhong.coolweather.util.Utility;

/**
 * Created by jinhong on 20/4/16.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温1
     */
    private TextView temp1Text;
    /**
     * 用于显示气温2
     */
    private TextView temp2Text;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /**
     * 切换城市按钮
     */
    private Button switchCity;

    /**
     * 更新天气按钮
     */
    private Button refreshWeather;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        init();


    }
    private void init(){
        weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);

        switchCity =(Button)findViewById(R.id.switch_city);
        refreshWeather= (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
        String countyCode = getIntent().getStringExtra("county_code");
        if(!TextUtils.isEmpty(countyCode)){
            //有县级代号就去查询天气
            publishText.setText("同步中。。。");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else{
            //没有县级代码时直接显示本地天气
            showWeather();
        }
    }
    /**
     * 查询县级代码所对应的天气代号
     */
    private void queryWeatherCode(String countyCode){
        String address ="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        queryFromServer(address,"countyCode");
    }
    /**
     * 查询天气代号所对应的天气
     */
    private void queryWeatherInfo(String weatherCode){
        String address ="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        queryFromServer(address, "weatherCode");
    }
    /**
     * 根据传入的地址和类型去向服务器查询天气代码或者天气信息
     */
    private void queryFromServer(final String address,final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器返回的数据中解析出天气代码
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }
    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示在界面上
     */
    private void showWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String cityName = prefs.getString("city_name", "");
        String minTemp = prefs.getString("temp2", "");
        String maxTemp = prefs.getString("temp1","");
        String weatherDesp = prefs.getString("weather_desp","");
        String publishTime = prefs.getString("publish_time","");
        String currentDate = prefs.getString("current_date", "");
        cityNameText.setText(cityName);
        temp1Text.setText(minTemp);
        temp2Text.setText(maxTemp);
        weatherDespText.setText(weatherDesp);
        publishText.setText(publishTime+"发布");
        currentDateText.setText(currentDate);
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

        //应该在后台Service运行，但为了看到效果把它放在这里。
        //LargeIcon必须是Bitmap
        Bitmap btm = BitmapFactory.decodeResource(getResources(),R.drawable.logo);
        Intent openintent = new Intent(this, WeatherActivity.class);
        //当点击消息时就会向系统发送openintent意图
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openintent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(WeatherActivity.this).setSmallIcon(R.drawable.logo)
                .setContentTitle("jinhom's weather")
                .setContentText(cityName + "天气实况" + "\n" + minTemp + "~" + maxTemp + weatherDesp).setTicker("weather news") //第一次提示消息显示在通知栏
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent);
                //.setLargeIcon(btm).setAutoCancel(true); //自己维护通知的消息
        Notification notification = mBuilder.build();
        notification.defaults = Notification.DEFAULT_SOUND;//发出默认声音
        //notification.flags = Notification.FLAG_AUTO_CANCEL;//点击通知后自动清除通知
        //获取通知管理器
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notification);//第一个参数为自定义的通知唯一标识

        // 第一个参数为图标,第二个参数为短暂提示标题,第三个为通知时间，这个方法已经过时了。
        //Notification notification = new Notification(R.drawable.logo, "jinhom weather comes", when);

        //启动后台的自动更新天气服务
        Intent intent = new Intent(this,AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.switch_city:
                Intent intent = new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中。。。");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code","");
                if(!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }
}
