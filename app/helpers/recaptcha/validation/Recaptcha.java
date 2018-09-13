package helpers.recaptcha.validation;

import net.sf.oval.configuration.annotation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This field is required.
 * Message key: validation.recaptcha
 * $1: field name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = RecaptchaCheck.class)
public @interface Recaptcha {

    String message() default RecaptchaCheck.mes;
}

