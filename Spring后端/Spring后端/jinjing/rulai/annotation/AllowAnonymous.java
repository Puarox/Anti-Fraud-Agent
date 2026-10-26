package rulai.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记允许匿名访问的接口（跳过JWT验证）
 */
@Target({ElementType.METHOD, ElementType.TYPE}) // 支持类和方法级别
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowAnonymous {
}
