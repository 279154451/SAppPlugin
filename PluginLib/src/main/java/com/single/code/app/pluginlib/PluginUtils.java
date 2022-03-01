package com.single.code.app.pluginlib;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 创建时间：2022/3/1
 * 创建人：singleCode
 * 功能描述：
 **/
public class PluginUtils {
    private static String TAG = "PluginUtils";
    public final static String apkPath = "/sdcard/pluginapp-debug.apk";
    private static Resources mResources;
    private static String pluginApk;
    public static void loadClass(Context context,String apkPath) {
        pluginApk = apkPath;
        File file = new File(apkPath);
        if(file != null && file.exists()){
            Log.e(TAG,"apkFile exists");
        }else {
            Log.e(TAG,"apkFile not exists "+file);
        }
        try {
            // private final DexPathList pathList;
            Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            // private Element[] dexElements;
            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            /**
             * 插件
             */
            // 创建插件的 DexClassLoader 类加载器，然后通过反射获取插件的 dexElements 值
            // 插件的
            DexClassLoader dexClassLoader = new DexClassLoader(apkPath, context.getCacheDir().getAbsolutePath(),
                    null, context.getClassLoader());

            Object pluginPathList = pathListField.get(dexClassLoader);
            // 拿到了插件的 dexElements
            Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);

            /**
             * 宿主
             */
            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();

            Object hostPathList = pathListField.get(pathClassLoader);
            // 拿到了宿主的 dexElements
            Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);


            /**
             * 创建数组
             */
            // Element[]
            Object[] dexElements = (Object[]) Array.newInstance(pluginDexElements.getClass().getComponentType(),
                    hostDexElements.length + pluginDexElements.length);

            System.arraycopy(hostDexElements, 0, dexElements, 0, hostDexElements.length);
            System.arraycopy(pluginDexElements, 0, dexElements, hostDexElements.length, pluginDexElements.length);

            // 将创建的 dexElements 放到宿主的 dexElements中
            // 宿主的dexElements = 新的dexElements
            dexElementsField.set(hostPathList, dexElements);
            Log.e(TAG,"loadClass apkPath="+apkPath);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"loadClass apkPath="+apkPath+" "+e.toString());
        }
    }

    /**
     * 获取插件的资源
     * @param context 这里的Context不能是Activity的，要是Application的，否则会循环获取，导致奔溃
     * @return
     */
    public static Resources getResources(Context context){
        if(mResources == null && pluginApk != null){
            mResources = loadResource(context,pluginApk);
        }
        return mResources;
    }
    /**
     * 加载外部的资源文件
     * @param context
     * @param apkPath
     * @return
     */
    private static Resources loadResource(Context context, String apkPath) {
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
