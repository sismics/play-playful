package helpers.check;

import net.sf.oval.configuration.annotation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This field is alphanumerical.
 * Message key: validation.alphanumerical
 * $1: field name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = AlphanumericalCheck.class)
public @interface Alphanumerical {
    enum Format {
        AZ09,
        AZ09DASH
    }

    String message() default "";
    Format format() default Format.AZ09;
}

