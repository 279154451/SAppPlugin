package com.single.code.app.pluginapp;

import android.app.Activity;
import android.content.res.Resources;

import com.single.code.app.pluginlib.PluginUtils;

/**
 * 创建时间：2022/3/1
 * 创建人：singleCode
 * 功能描述：
 **/
public class BaseActivity extends Activity {
    @Override
    public Resources getResources() {
        Resources resources = PluginUtils.getResources(getApplication());
        return resources == null ? super.getResources() : resources;
    }
}
