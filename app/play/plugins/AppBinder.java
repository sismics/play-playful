package play.plugins;

import helpers.binding.DateBinder;
import helpers.binding.StringBinder;
import helpers.binding.UUIDBinder;
import play.PlayPlugin;
import play.data.binding.Binder;

import java.util.Date;
import java.util.UUID;

/**
 * @author jtremeaux
 */
public class AppBinder extends PlayPlugin {
    @Override
    public void afterApplicationStart() {
        Binder.register(Date.class, new DateBinder());
        Binder.register(String.class, new StringBinder());
        Binder.register(UUID.class, new UUIDBinder());
    }
}
