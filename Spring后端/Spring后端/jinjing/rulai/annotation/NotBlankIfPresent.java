package rulai.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;
/**
 * <p>
 * 自定义校验注解
 * </p>
 *
 * @author Mistry
 * @since 2025-06-26
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankIfPresent.Validator.class)
@Documented
public @interface NotBlankIfPresent {
    String message() default "字段不为null时不能为空";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    class Validator implements ConstraintValidator<NotBlankIfPresent, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || !value.trim().isEmpty();
        }
    }
}