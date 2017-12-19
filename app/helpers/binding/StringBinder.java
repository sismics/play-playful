package helpers.binding;

import com.google.common.base.Strings;
import play.data.binding.TypeBinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A binder that removes whitespace from strings.
 *
 * @author jtremeaux
 */
public class StringBinder implements TypeBinder<String> {
    public String bind(String name, Annotation[] annotations, String value, Class actualClass, Type genericType) throws Exception {
        if (value == null) {
            return null;
        }
        return Strings.emptyToNull(value.trim());
    }
}

