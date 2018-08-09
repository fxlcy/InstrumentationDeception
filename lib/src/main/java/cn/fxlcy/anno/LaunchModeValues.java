package cn.fxlcy.anno;

import android.app.Activity;

import cn.fxlcy.stub.ActivityStub;

/**
 * Created by fxlcy on 18-8-9.
 */

public enum LaunchModeValues {

    STANDARD(ActivityStub.Standard.class),
    SINGLE_TOP(ActivityStub.SingleTop.class),
    SINGLE_TASK(ActivityStub.SingleTask.class),
    SINGLE_INSTANCE(ActivityStub.SingleInstance.class);

    private Class<? extends Activity> activityType;

    public Class<? extends Activity> getActivityType() {
        return activityType;
    }

    LaunchModeValues(Class<? extends Activity> activityType) {
        this.activityType = activityType;
    }
}
