package com.single.code.app.pluginapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.single.code.app.pluginlib.PluginUtils;

import java.lang.reflect.Field;

/**
 * 创建时间：2022/3/1
 * 创建人：singleCode
 * 功能描述：
 **/
public class BaseActivity extends AppCompatActivity {
    protected Context mContext;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resource = PluginUtils.getResources(getApplication());
        if(resource != null){
            // 替换这个 context 的resource 为我们自己写的 resource，外面通过mContext获取到的就是插件的资源了。这样能够解决插件的资源与宿主资源冲突
            mContext = new ContextThemeWrapper(getBaseContext(), 0);

            Class<? extends Context> clazz = mContext.getClass();
            try {
                Field mResourcesField = clazz.getDeclaredField("mResources");
                mResourcesField.setAccessible(true);
                mResourcesField.set(mContext, resource);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else {
            mContext = this;
        }
    }
//    @Override
//    public Resources getResources() {
//        Resources resources = PluginUtils.getResources(getApplication());
//        return resources == null ? super.getResources() : resources;//这样方式的缺点时无法解决插件与宿主资源的冲突
//    }
}
