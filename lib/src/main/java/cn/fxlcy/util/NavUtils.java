package cn.fxlcy.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by fxlcy on 18-8-8.
 */

public class NavUtils {

    public static boolean isRegisterOnManifest(Context context, ComponentName componentName) {
        try {
            return !context.getPackageManager().getActivityInfo(componentName, PackageManager.GET_META_DATA).metaData
                    .containsKey("placeholder");
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
