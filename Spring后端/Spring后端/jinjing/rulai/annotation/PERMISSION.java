package rulai.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // 支持类和方法级别
@Retention(RetentionPolicy.RUNTIME)
public @interface PERMISSION {
    String[] permissions() default {};
}
