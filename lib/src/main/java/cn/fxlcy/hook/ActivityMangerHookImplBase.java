package cn.fxlcy.hook;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.fxlcy.stub.ActivityStub;
import cn.fxlcy.util.Lazy;
import cn.fxlcy.util.NavUtils;
import cn.fxlcy.util.ReflectUtils;

/**
 * Created by fxlcy on 18-8-8.
 */

public class ActivityMangerHookImplBase implements IIActivityMangerHook {
    private Context mContext;
    private Field mSingletonField;
    private Object mDefaultInstance;
    private Lazy<Class<?>> mActivityThreadClass = new Lazy<>(new Lazy.SimpleGetter<Class<?>>() {
        @SuppressLint("PrivateApi")
        @Override
        public Class<?> get() throws Throwable {
            return Class.forName("android.app.ActivityThread");
        }
    });

    private Lazy<Field> mCurrentThreadField = new Lazy<>(new Lazy.SimpleGetter<Field>() {
        @Override
        public Field get() throws Throwable {
            Class<?> activityThreadClass = mActivityThreadClass.get();
            Field currentThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            currentThreadField.setAccessible(true);
            return currentThreadField;
        }
    });

    private Lazy<Object> mCurrentThread = new Lazy<>(new Lazy.SimpleGetter<Object>() {
        @Override
        public Object get() throws Throwable {
            return mCurrentThreadField.get().get(null);
        }
    });

    private Lazy<Object> mIPackageManager = new Lazy<>(new Lazy.SimpleGetter<Object>() {
        @Override
        public Object get() throws Throwable {
            try {
                Method getPackageManager;
                getPackageManager = mActivityThreadClass.get().getDeclaredMethod("getPackageManager");
                getPackageManager.setAccessible(true);
                return getPackageManager.invoke(mCurrentThread.get());
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return null;

        }
    });

    public static final String KEY_EXTRA_TARGET_INTENT = "EXTRA_TARGET_INTENT";

    public ActivityMangerHookImplBase(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }


    @Override
    public Object getIActivityManger() {
        init();
        //传入gDefault对象实例得到IActivityManager对象实例
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void hookIActivityManger() {
        Object am = getIActivityManger();
        if (am == null) {
            throw new NullPointerException("IActivityManger");
        }

        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), am.getClass().getInterfaces(),
                new IActivityMangerProxy(am));
        hookIActivityManger(proxy);
    }


    @SuppressLint("PrivateApi")
    @Override
    public void hookLaunch() {
        try {
            Class<?> activityThreadClass = mActivityThreadClass.get();
            Object currentThread = mCurrentThread.get();
            Field hField = activityThreadClass.getDeclaredField("mH");
            hField.setAccessible(true);
            Handler handler = (Handler) hField.get(currentThread);
            Field callbackField = Handler.class.getDeclaredField("mCallback");
            callbackField.setAccessible(true);
            callbackField.set(handler, new LaunchCallback());


            //绕过AppCompatActivity的验证
            PackageManagerHandler packageManagerHandler = new PackageManagerHandler(mIPackageManager.get());
            Class<?> iPackageManagerIntercept = Class.forName("android.content.pm.IPackageManager");
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{iPackageManagerIntercept}, packageManagerHandler);
            // 获取 sPackageManager 属性
            Field iPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            iPackageManagerField.setAccessible(true);
            iPackageManagerField.set(mCurrentThreadField.get(), proxy);
        } catch (Throwable ignored) {
        }
    }


    @Override
    public void hook() {
        hookIActivityManger();
        hookLaunch();
    }


    @SuppressLint("PrivateApi")
    private void init() {
        if (mSingletonField == null || mDefaultInstance == null) {
            try {
                Class<?> ams_class = Class.forName("android.app.ActivityManagerNative");
                Field gDefault = ams_class.getDeclaredField("gDefault");
                gDefault.setAccessible(true);
                mDefaultInstance = gDefault.get(null);
                //单例工具类的一个属性对象保存的是IActivityManager对象实例
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


    private final static class LaunchCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                //启动activity的what
                case 100: {
                    //得到ActivityClientRecord
                    Object obj = msg.obj;
                    try {
                        //得到Intent对象
                        Field intent_field = obj.getClass().getDeclaredField("intent");
                        intent_field.setAccessible(true);
                        Intent intent = (Intent) intent_field.get(obj);
                        //取出我们前面存在Intent里的原本没有注册在清单文件的Activity的Intent
                        Intent target_intent = intent.getParcelableExtra(KEY_EXTRA_TARGET_INTENT);

                        if (target_intent != null) {
                            intent.setComponent(target_intent.getComponent());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }

            return false;
        }
    }

    private final class PackageManagerHandler implements InvocationHandler {
        private Object iPackageManager;

        PackageManagerHandler(Object iPackageManager) {
            this.iPackageManager = iPackageManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("getActivityInfo".equals(method.getName())) {
                try {
                    return method.invoke(iPackageManager, args);
                } catch (Throwable e) {
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof ComponentName) {
                            ComponentName componentName = new ComponentName(mContext.getPackageName()
                                    , ActivityStub.Standard.class.getName());
                            args[i] = componentName;
                        }
                    }
                }

            }
            return method.invoke(iPackageManager, args);
        }
    }

    private class IActivityMangerProxy implements InvocationHandler {
        private final static String TAG = "IActivityMangerProxy";

        private Object mActivityManger;

        public IActivityMangerProxy(Object activityManager) {
            this.mActivityManger = activityManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("startActivity")) {
                //获取调用此方法传入的参数 我们这里只要Intent替换，所以只需要Intent替换
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        Intent intent = (Intent) arg;
                        Log.i("TAG", intent.getComponent().getClassName());
                        if (NavUtils.isRegisterOnManifest(mContext, intent.getComponent())) {
                            Log.i("TAG", intent.getComponent().getClassName() + "true");
                            return method.invoke(mActivityManger, args);
                        }
                        intent = (Intent) ((Intent) arg).clone();
                        ComponentName componentName = new ComponentName(mContext
                                , ActivityStub.Standard.class);
                        intent.setComponent(componentName);
                        intent.putExtra(KEY_EXTRA_TARGET_INTENT, ((Intent) arg));
                        args[i] = intent;
                        return method.invoke(mActivityManger, args);
                    }
                }
            }

            return method.invoke(mActivityManger, args);
        }
    }

    private static IIActivityMangerHook sImpl;
    private final static Object sLock = new Object();

    public static IIActivityMangerHook getImpl(Context context) {
        if (sImpl == null) {
            synchronized (sLock) {
                if (sImpl == null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        sImpl = new ActivityMangerHookImpl26(context);
                    } else {
                        sImpl = new ActivityMangerHookImplBase(context);
                    }
                }
            }
        }


        return sImpl;
    }


    public static void hook(Context context) {
        getImpl(context).hook();
    }


}
