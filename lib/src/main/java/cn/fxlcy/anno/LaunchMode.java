package cn.fxlcy.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by fxlcy on 18-8-9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LaunchMode {
    LaunchModeValues value() default LaunchModeValues.STANDARD;
}
