package cn.fxlcy.demo;

import android.app.Application;
import android.content.Context;

import cn.fxlcy.hook.ActivityMangerHookImplBase;

/**
 * Created by fxlcy on 18-8-8.
 */

public class App extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ActivityMangerHookImplBase.hook(base);
    }
}
