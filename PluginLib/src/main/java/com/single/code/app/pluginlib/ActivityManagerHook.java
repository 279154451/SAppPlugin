package com.single.code.app.pluginlib;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 创建时间：2022/2/27
 * 创建人：singleCode
 * 功能描述：
 **/
public class ActivityManagerHook {
    private static final String TAG ="ActivityManagerHook";
    private static final String TARGET_INTENT = "target_intent";
    public static void hookAms(){
        try {

            // 获取Singleton对象
            Class<?> clazz = Class.forName("android.app.ActivityManager");
            Field singletonField = clazz.getDeclaredField("IActivityManagerSingleton");
            singletonField.setAccessible(true);
            Object singleton = singletonField.get(null);

            // 获取IActivityManager 对象
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(singleton);


            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");

            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass}, new InvocationHandler() {

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            // IActivityManager 的方法执行的时候，都会先跑这儿
                            Log.d(TAG,"methodName="+method.getName());
                            if ("startActivity".equals(method.getName())) {
                                // 替换Intent
                                int index = 0;

                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        index = i;
                                        break;
                                    }
                                }
                                // 启动插件的intent
                                Intent intent = (Intent) args[index];

                                Intent proxyIntent = new Intent();
                                //这里的packageName要填Activity的主工程的包名，也就是应用包名，而不是Activity类的包名
                                proxyIntent.setClassName("com.single.code.app.plugin",
                                        "com.single.code.app.pluginlib.PluginProxyActivity");

                                // 保存原来的
                                proxyIntent.putExtra(TARGET_INTENT, intent);
                                Log.e(TAG, "startActivity: proxyIntent intent ="+proxyIntent);
                                Log.e(TAG, "startActivity: target intent ="+intent);
                                args[index] = proxyIntent;
                            }
                            return method.invoke(mInstance, args);
                        }

                    });

            // 替换系统的 IActivityManager对象
            mInstanceField.set(singleton, proxyInstance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void hookHandler(){
        try {
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            Object activityThread = sCurrentActivityThreadField.get(null);

            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            Object mH = mHField.get(activityThread);

            // new 一个 Callback 替换系统的 mCallback对象
            Class<?> handlerClass = Class.forName("android.os.Handler");
            Field mCallbackField = handlerClass.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(mH, new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    // 将Intent换回来
                    Log.e(TAG, "handleMessage:"+msg);
                    switch (msg.what) {
                        case 100:
                            try {
                                // 获取ActivityClientRecord中的intent对象
                                Field intentField = msg.obj.getClass().getDeclaredField("intent");
                                Log.e(TAG, "handleMessage: 1111111111111");
                                intentField.setAccessible(true);
                                Intent proxyIntent = (Intent) intentField.get(msg.obj);

                                // 拿到插件的Intent
                                Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                               if(intent != null){
                                   Log.e(TAG, "handleMessage: " + intent);
                                   // 替换回来
                                   proxyIntent.setComponent(intent.getComponent());
                               }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case 159:
                            try {
                                Class<?> clazz = Class.forName("android.app.servertransaction.ClientTransaction");
                                Field mActivityCallbacksField = clazz.getDeclaredField("mActivityCallbacks");
                                mActivityCallbacksField.setAccessible(true);

                                List activityCallbacks = (List) mActivityCallbacksField.get(msg.obj);
                                for (int i = 0; i < activityCallbacks.size(); i++) {
                                    if (activityCallbacks.get(i).getClass().getName()
                                            .equals("android.app.servertransaction.LaunchActivityItem")) {
                                        Object launchActivityItem = activityCallbacks.get(i);
                                        Field mIntentField = launchActivityItem.getClass().getDeclaredField("mIntent");
                                        mIntentField.setAccessible(true);
                                        Intent proxyIntent = (Intent) mIntentField.get(launchActivityItem);
                                        Log.e(TAG, "handleMessage: proxyIntent intent ="+proxyIntent);
                                        //插件的intent
                                        Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                        Log.e(TAG, "handleMessage: target intent ="+intent);
                                        if (intent != null) {
                                            mIntentField.set(launchActivityItem, intent);
                                        }
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载外部的资源文件
     * @param context
     * @param apkPath
     * @return
     */
    public static Resources loadResource(Context context,String apkPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();

            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            // 参数就是插件的资源路径
            addAssetPathMethod.invoke(assetManager, apkPath);

            Resources resources = context.getResources();

            return new Resources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
