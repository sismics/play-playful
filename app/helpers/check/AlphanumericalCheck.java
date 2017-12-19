package helpers.check;

import com.google.common.collect.ImmutableMap;
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.regex.Pattern;

public class AlphanumericalCheck extends AbstractAnnotationCheck<Alphanumerical> {
    private Alphanumerical.Format format;

    final Map<Alphanumerical.Format, Pattern> FORMAT = ImmutableMap.of(
            Alphanumerical.Format.AZ09, Pattern.compile("^[a-z][a-z0-9]*$"),
            Alphanumerical.Format.AZ09DASH, Pattern.compile("^[a-z][a-z0-9\\-]*$"));

    final Map<Alphanumerical.Format, String> MESSAGE = ImmutableMap.of(
            Alphanumerical.Format.AZ09, "validation.alphanumerical",
            Alphanumerical.Format.AZ09DASH, "validation.alphanumericalDash");

    public static final String mes = "validation.alphanumerical";

    @Override
    public void configure(Alphanumerical alphanumerical) {
        this.format = alphanumerical.format();
        String message = alphanumerical.message();
        if ("".equals(message)) {
            message = MESSAGE.get(format);
        }
        setMessage(message);
    }

    @Override
    public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) {
        if (value == null || !(value instanceof String)) {
            return true;
        }
        String s = StringUtils.trim((String) value);
        if (s.length() == 0) {
            return true;
        }

        return FORMAT.get(format).matcher(s).matches();
    }
}
