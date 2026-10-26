package rulai.annotation;

import rulai.constant.INTERFACE_CONSTANT;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * <p>
 * 注册表装饰器注解，标记需要注册的方法
 * </p>
 *
 * @author Mistry
 * @since 2025-01-06
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterpolationInterface {
    /**
     * 接口名称，默认使用方法名
     */
    String value() default "";
    
    /**
     * 接口描述
     */
    String displayName() default "";

    /**
     * 接口类型
     */
    INTERFACE_CONSTANT.TYPE type();
}