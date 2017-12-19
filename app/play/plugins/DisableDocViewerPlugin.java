package play.plugins;

import play.Play;
import play.PlayPlugin;
import play.mvc.Router;

import java.util.stream.Collectors;

/**
 * @author jtremeaux
 */
public class DisableDocViewerPlugin extends PlayPlugin {
    @Override
    public void onApplicationStart() {
        removePlugin();
        removeRoutes();
    }

    public void removeRoutes() {
        Router.routes.removeAll(Router.routes.stream()
                .filter(r -> r.path.startsWith("/@projectdocs") || r.path.startsWith("/@documentation"))
                .collect(Collectors.toSet()));
    }

    public void removePlugin() {
        for (PlayPlugin plugin : Play.pluginCollection.getEnabledPlugins()) {
            if (plugin.toString().startsWith("DocViewerPlugin")) {
                Play.pluginCollection.disablePlugin(plugin);
            }
        }
    }
}
