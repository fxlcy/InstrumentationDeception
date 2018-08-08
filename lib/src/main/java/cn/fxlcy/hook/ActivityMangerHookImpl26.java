package cn.fxlcy.hook;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Field;

import cn.fxlcy.util.ReflectUtils;

/**
 * Created by fxlcy on 18-8-8.
 */

public class ActivityMangerHookImpl26 extends ActivityMangerHookImplBase {
    private Field mSingletonField;
    private Object mDefaultInstance;

    public ActivityMangerHookImpl26(Context context) {
        super(context);
    }

    @SuppressLint("PrivateApi")
    @Override
    public Object getIActivityManger() {
        init();
        try {
            return mSingletonField.get(mDefaultInstance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void hookIActivityManger(Object activityManager) {
        init();
        try {
            mSingletonField.set(mDefaultInstance, activityManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        if (mSingletonField == null || mDefaultInstance == null) {

            try {
                Class<?> ams_class = Class.forName("android.app.ActivityManager");
                Field singletonField = ams_class.getDeclaredField("IActivityManagerSingleton");
                singletonField.setAccessible(true);
                mDefaultInstance = singletonField.get(null);
                mSingletonField = ReflectUtils.getSuperClass(mDefaultInstance.getClass(), new ReflectUtils.Predicate<Class<?>>() {
                    @Override
                    public boolean test(Class<?> obj) {
                        return "android.util.Singleton".equals(obj.getName());
                    }
                }).getDeclaredField("mInstance");
                mSingletonField.setAccessible(true);
            } catch (Throwable ignored) {
            }
        }
    }


}
