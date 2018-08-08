package cn.fxlcy.hook;

/**
 * Created by fxlcy on 18-8-8.
 */

public interface IIActivityMangerHook {

    Object getIActivityManger();

    void hookIActivityManger(Object activityManager);

    void hookLaunch();

    void hookIActivityManger();

    void hook();
}
