import com.jayfella.plugin.manager.SimplePluginLoader;
import com.jayfella.plugin.manager.SimplePluginManager;
import com.jayfella.plugin.manager.plugin.Plugin;
import com.jayfella.plugin.manager.plugin.SimplePlugin;

import java.io.File;
import java.util.logging.Logger;

public class TestPluginManager {

    private static final Logger log = Logger.getLogger(TestPluginManager.class.getName());

    public static void main(String... args) {

        SimplePluginManager pluginManager = new SimplePluginManager();
        pluginManager.registerInterface(SimplePluginLoader.class);

        File pluginDir = new File("./plugins");

        Plugin[] plugins = pluginManager.loadPlugins(pluginDir);
        pluginManager.enablePlugins();

        log.info("Loaded Plugins(s): " + plugins.length);
        log.info("Disabled Plugin(s): " + pluginManager.getDisabledPluginCount());
        log.info("Not Loaded Plugin(s): " + pluginManager.getPluginsNotLoadedCount());

        for (Plugin plugin : plugins) {

            log.info(String.format("Plugin: [ Name: %s, Type: %s, Enabled: %b ]",
                    plugin.getDescription().getId(),
                    plugin.getDescription().getType(),
                    plugin.isEnabled()
            ));

        }

        log.info("Test Complete.");

    }

    private static class TestPlugin extends SimplePlugin {

        @Override
        public void onLoad() {

        }

        @Override
        public void onUnload() {

        }

        @Override
        public void onEnable() {

        }

        @Override
        public void onDisable() {

        }

    }

}
