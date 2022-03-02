package com.single.code.app.pluginapp;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_main,null);//这里需要使用新创建的Context来获取插件资源
        setContentView(view);
    }

}