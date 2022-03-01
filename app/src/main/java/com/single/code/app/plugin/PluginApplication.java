package com.single.code.app.plugin;

import android.app.Application;

import com.single.code.app.pluginlib.ActivityManagerHook;
import com.single.code.app.pluginlib.PluginUtils;

/**
 * 创建时间：2022/2/27
 * 创建人：singleCode
 * 功能描述：
 **/
public class PluginApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PluginUtils.loadClass(this,PluginUtils.apkPath);
        ActivityManagerHook.hookAms();
        ActivityManagerHook.hookHandler();
    }
}
