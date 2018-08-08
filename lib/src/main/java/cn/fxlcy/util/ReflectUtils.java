package cn.fxlcy.util;

import android.support.annotation.NonNull;

/**
 * Created by fxlcy on 18-8-8.
 */

public class ReflectUtils {

    public static Class<?> getSuperClass(@NonNull Class<?> baseClass, Predicate<Class<?>> predicate) {
        Class<?> c = baseClass;

        do {
            if (predicate.test(c)) {
                return c;
            }
            c = c.getSuperclass();
        } while (c != null);

        throw new NoClassDefFoundError(baseClass.getCanonicalName());
    }


    public interface Predicate<T> {
        boolean test(T obj);
    }
}
