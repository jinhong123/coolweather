package com.jinhong.coolweather.util;

/**
 * Created by jinhong on 20/4/16.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
