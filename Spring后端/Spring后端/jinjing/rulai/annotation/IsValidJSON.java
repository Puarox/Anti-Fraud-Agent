package rulai.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@Constraint(validatedBy = IsValidJSON.Validator.class)
@Documented
public @interface IsValidJSON {
    String message() default "该字段必须是有效JSON";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    public class Validator implements ConstraintValidator<IsValidJSON, String> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }

            if (value.trim().isEmpty()) {
                return true;
            }

            try {
                objectMapper.readTree(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}