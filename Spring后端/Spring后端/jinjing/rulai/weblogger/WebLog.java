package rulai.weblogger;

import java.lang.annotation.*;

/**
 * <p>
 * 自定义注解
 * 功能：输出日志
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

@Retention(RetentionPolicy.RUNTIME) //何时使用该注解，此处为运行时
@Target({ElementType.METHOD}) //注解用于何处，此处为作用于方法上
@Documented //注解是否将包含在 JavaDoc 中
public @interface WebLog {

    /**
     * 属性
     * 日志描述信息
     * 默认为空字符串
     * @return
     */
    String description() default "";
}