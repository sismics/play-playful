package play.plugins;

import play.PlayPlugin;

import java.util.TimeZone;

/**
 * @author jtremeaux
 */
public class UTCTimeZonePlugin extends PlayPlugin {
    @Override
    public void afterApplicationStart() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
