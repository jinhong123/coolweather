package com.jinhong.coolweather.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jinhong.coolweather.service.AutoUpdateService;

/**
 * Created by jinhong on 17/5/16.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startActivity(i);
    }

}
