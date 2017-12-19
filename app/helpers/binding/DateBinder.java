package helpers.binding;

import helpers.extension.PlayfulJavaExtensions;
import play.data.binding.TypeBinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * A binder for ISO dates.
 *
 * @author jtremeaux
 */
public class DateBinder implements TypeBinder<Date> {
    public Date bind(String name, Annotation[] annotations, String value, Class actualClass, Type genericType) throws Exception {
        return PlayfulJavaExtensions.parseDateTimeIso(value);
    }
}

