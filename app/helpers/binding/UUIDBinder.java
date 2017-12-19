package helpers.binding;

import play.data.binding.TypeBinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * A binder for UUIDs.
 *
 * @author jtremeaux
 */
public class UUIDBinder implements TypeBinder<UUID> {
    public UUID bind(String name, Annotation[] annotations, String value, Class actualClass, Type genericType) throws Exception {
        if (value == null) {
            return null;
        }
        return UUID.fromString(value.trim());
    }
}

