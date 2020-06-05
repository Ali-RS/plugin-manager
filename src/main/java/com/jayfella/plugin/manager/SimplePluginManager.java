package com.jayfella.plugin.manager;

import com.jayfella.plugin.manager.exception.DependencyNotFoundException;
import com.jayfella.plugin.manager.exception.InvalidPluginDescriptionException;
import com.jayfella.plugin.manager.exception.InvalidPluginException;
import com.jayfella.plugin.manager.json.JsonObjectMapper;
import com.jayfella.plugin.manager.plugin.Plugin;
import com.jayfella.plugin.manager.plugin.description.PluginDescription;
import com.jayfella.plugin.manager.sorter.PluginSorter;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimplePluginManager implements PluginManager {

    private static final Logger log = Logger.getLogger("Plugin Manager");

    private final Map<String, PluginLoader> pluginLoaders = new HashMap<>();
    private final List<Plugin> plugins = new ArrayList<>();
    private long pluginsNotLoadedCount = 0;

    private final ClassSerializer classSerializer = new ClassSerializer();

    public SimplePluginManager() {
    }

    @Override
    public void registerInterface(@NotNull Class<? extends PluginLoader> loader) {

        PluginLoader instance;

        if (PluginLoader.class.isAssignableFrom(loader)) {
            Constructor<? extends PluginLoader> constructor;

            try {
                constructor = loader.getConstructor();
                instance = constructor.newInstance();
            } catch (NoSuchMethodException ex) {
                String className = loader.getName();

                throw new IllegalArgumentException(String.format("Class %s does not have a public %s(PluginSystem) constructor", className, className), ex);
            } catch (Exception ex) {
                throw new IllegalArgumentException(String.format("Unexpected exception %s while attempting to construct a new instance of %s", ex.getClass().getName(), loader.getName()), ex);
            }
        } else {
            throw new IllegalArgumentException(String.format("Class %s does not implement interface PluginLoader", loader.getName()));
        }

        pluginLoaders.put(instance.getTypeName(), instance);
        log.info("Registered Interface: " + loader.getSimpleName());
    }

    @Override
    public @NotNull Logger getLogger() {
        return log;
    }

    @Override
    @Nullable
    public Plugin loadPlugin(@Nullable File file) throws InvalidPluginException, InvalidPluginDescriptionException, NullPointerException {

        if (file == null) {
            throw new NullPointerException("File cannot be null.");
        } else if (!file.exists()) {
            throw new InvalidPluginException(new FileNotFoundException("Plugin " + file.getPath() + " does not exist."));
        }

        Plugin plugin = null;

        boolean isJar = FilenameUtils.isExtension(file.getName(), "jar");

        if (isJar) {

            // Get the description so we can determine what type of plugin it is.
            // This will allow us to determine which pluginLoader we need.

            PluginDescription description;
            PluginLoader pluginLoader;

            description = getPluginDescription(file);

            description.validate();

            if (description != null) {

                pluginLoader = pluginLoaders.get(description.getType());

                if (description.getType() == null || description.getType().trim().isEmpty()) {
                    throw new InvalidPluginDescriptionException("No 'type' property set in plugin.json");
                }

                if (pluginLoader == null) {
                    throw new InvalidPluginDescriptionException("Unknown type '" + description.getType() + "' in plugin.json");
                }

                plugin = pluginLoader.loadPlugin(this, file, description);

                // plugin.onLoad();

            }
        }

        return plugin;
    }

    @Override
    public Plugin[] loadPlugins(File directory) {

        List<Plugin> loadedPlugins = new ArrayList<>();

        for (File pluginFile : directory.listFiles()) {

            try {

                Plugin plugin = loadPlugin(pluginFile);

                if (plugin != null) {

                    log.info("Loading " + plugin.getDescription().getId());
                    plugin.onLoad();

                    loadedPlugins.add(plugin);
                }

            } catch (Throwable e) {
                log.log(Level.WARNING, "Unable to load plugin '" + pluginFile.getPath() + "' - " + e.getCause().getMessage(), e);
                pluginsNotLoadedCount++;
            }

        }

        // At this point the plugin has passed all checks and is considered a valid plugin.
        // It may still be removed if it depends on plugins that do not exist.
        plugins.addAll(loadedPlugins);

        plugins.removeIf(plugin -> {

            // check for softDependencies
            // this only gives the user a log warning that functionality may be limited.
            checkSoftDependencies(plugin);

            // Check for dependencies that don't exist.
            try {
                checkDependencies(plugin);
            } catch (DependencyNotFoundException e) {
                log.warning(e.getMessage());
                return true;
            }

            return false;
        });

        // Check for cyclic dependencies
        // This can potentially terminate the application if cyclic dependencies exist.
        // The user must resolve the issue or remove the offending plugin.
        PluginSorter.sort(plugins);

        return loadedPlugins.toArray(new Plugin[0]);
    }

    @Override
    public void enablePlugins() {

        for (Plugin plugin : plugins) {

            if (!plugin.isEnabled()) {
                try {

                    log.info("Enabling " + plugin.getDescription().getId());
                    plugin.onEnable();
                    plugin.setEnabled(true);

                } catch (Throwable ex) {
                    log.warning("Error occurred (in the plugin loader) while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)");
                    ex.printStackTrace();
                }

            }

        }

    }

    private void checkSoftDependencies(Plugin plugin) {

        final List<String> dependenciesNotFound = new ArrayList<>();

        for (String strDep : plugin.getDescription().getDependencies()) {

            Plugin dependency = getPlugin(strDep);

            if (dependency == null) {
                dependenciesNotFound.add(strDep);
            }

        }

        if (!dependenciesNotFound.isEmpty()) {

            StringBuilder message = new StringBuilder();

            String depsNotFoundString = String.join(", ", dependenciesNotFound);

            message.append("Plugin '")
                    .append(plugin.getDescription().getId())
                    .append("' may have limited functionality because it soft-depends on plugins that do not exist: [ ")
                    .append(depsNotFoundString)
                    .append(" ]");

            dependenciesNotFound.clear();

            log.warning(message.toString());
        }

    }

    private void checkDependencies(Plugin plugin) throws DependencyNotFoundException {

        final List<String> dependenciesNotFound = new ArrayList<>();

        for (String strDep : plugin.getDescription().getDependencies()) {

            Plugin dependency = getPlugin(strDep);

            if (dependency == null) {
                dependenciesNotFound.add(strDep);
            }

        }

        if (!dependenciesNotFound.isEmpty()) {

            StringBuilder message = new StringBuilder();

            String depsNotFoundString = String.join(", ", dependenciesNotFound);

            message.append("Plugin '")
                    .append(plugin.getDescription().getId())
                    .append("' has been unloaded because it depends on plugin that do not exist: [ ")
                    .append(depsNotFoundString)
                    .append(" ]");

            dependenciesNotFound.clear();

            throw new DependencyNotFoundException(message.toString());
        }

    }

    @Override
    public @Nullable Plugin getPlugin(String name) {
        return plugins.stream()
                .filter(plugin -> plugin.getDescription().getId().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @NotNull Plugin[] getPlugins() {
        return plugins.toArray(new Plugin[0]);
    }

    @Override
    public long getLoadedPluginCount() {
        return plugins.size();
    }

    @Override
    public long getEnabledPluginCount() {
        return plugins.stream()
                .filter(Plugin::isEnabled)
                .count();
    }

    @Override
    public long getDisabledPluginCount() {
        return plugins.stream()
                .filter(plugin -> !plugin.isEnabled())
                .count();
    }

    @Override
    public long getPluginsNotLoadedCount() {
        return pluginsNotLoadedCount;
    }

    @Override
    public ClassSerializer getClassSerializer() {
        return classSerializer;
    }

    @Override
    public boolean isTransitiveDependency(@NotNull PluginDescription plugin, @NotNull PluginDescription depend) {

        // Preconditions.checkArgument(plugin != null, "plugin");
        // Preconditions.checkArgument(depend != null, "depend");

//        Validate.notNull(plugin, "plugin is null");
//        Validate.notNull(depend, "depend is null");
//
//        if (dependencyGraph.nodes().contains(plugin.getName())) {
//            if (Graphs.reachableNodes(dependencyGraph, plugin.getName()).contains(depend.getName())) {
//                return true;
//            }
//            for (String provided : depend.getProvides()) {
//                if (Graphs.reachableNodes(dependencyGraph, plugin.getName()).contains(provided)) {
//                    return true;
//                }
//            }
//        }
//        return false;

        // @TODO: provide the transitive dependency check.
        return true;
    }

    private PluginDescription getPluginDescription(@NotNull File file) throws InvalidPluginDescriptionException {

        JarFile jarFile = null;
        InputStream inputStream = null;

        try {

            jarFile = new JarFile(file);

            JarEntry jarEntry = jarFile.getJarEntry("plugin.json");

            if (jarEntry == null) {
                throw new InvalidPluginDescriptionException(new FileNotFoundException("Jar does not container plugin.json"));
            }

            inputStream = jarFile.getInputStream(jarEntry);

            try {
                return JsonObjectMapper.getInstance().getObjectMapper().readValue(inputStream, PluginDescription.class);
            } catch (IOException e) {
                throw new InvalidPluginDescriptionException(e);
            }

        } catch (IOException e) {
            // e.printStackTrace();
            throw new InvalidPluginDescriptionException(e);
        }
        finally {

            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
