package rulai.annotation;

import java.lang.annotation.*;
/**
 * <p>
 * 自动记录操作日志注解
 * 被此注解修饰的方法会被自动记录操作日志
 * </p>
 *
 * @author Mistry
 * @since 2025-01-06
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {
    
    /**
     * 操作类型
     * 如果不指定，默认会根据HTTP方法自动推断
     */
    String type() default "";
    
    /**
     * 操作的表名
     * 如果不指定，默认会根据请求参数或返回对象自动推断
     */
    String tableName() default "";
    
    /**
     * 操作描述
     * 可以用于补充说明操作内容
     */
    String description() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean recordRequest() default true;
    
    /**
     * 是否记录返回结果
     */
    boolean recordResponse() default true;
}