package com.single.code.app.plugin;

import android.app.Application;

import com.single.code.app.pluginlib.ActivityManagerHook;

/**
 * 创建时间：2022/2/27
 * 创建人：singleCode
 * 功能描述：
 **/
public class PluginApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActivityManagerHook.hookAms();
        ActivityManagerHook.hookHandler();
    }
}
