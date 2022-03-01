package com.single.code.app.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * 创建时间：2022/2/27
 * 创建人：singleCode
 * 功能描述：
 **/
public class PluginTes2tActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        Log.d("houqing","启动已注册Activity");
    }
}
