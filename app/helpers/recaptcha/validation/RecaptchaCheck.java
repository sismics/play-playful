package helpers.recaptcha.validation;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;

@SuppressWarnings("serial")
public class RecaptchaCheck extends AbstractAnnotationCheck<Recaptcha> {
    
    final static String mes = "validation.recaptcha";

    @Override
    public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) {
        if (value == null) {
            return false;
        }
        if (!(value instanceof String)) {
            return false;
        }
        return helpers.recaptcha.Recaptcha.get().verifyAndValidate((String) value);
    }
}
