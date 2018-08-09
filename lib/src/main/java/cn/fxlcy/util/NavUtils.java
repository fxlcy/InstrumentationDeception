package cn.fxlcy.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

/**
 * Created by fxlcy on 18-8-8.
 */

public class NavUtils {

    public final static String IS_REGISTER_ON_MANIFEST_TAG = "placeholder";

    public static boolean isRegisterOnManifest(Context context, ComponentName componentName) {
        try {
            Bundle metaData = context.getPackageManager().getActivityInfo(componentName, PackageManager.GET_META_DATA).metaData;
            return metaData == null || !metaData.containsKey(IS_REGISTER_ON_MANIFEST_TAG);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
