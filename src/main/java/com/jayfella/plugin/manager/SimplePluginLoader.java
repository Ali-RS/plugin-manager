package com.jayfella.plugin.manager;

import com.jayfella.plugin.manager.exception.InvalidPluginException;
import com.jayfella.plugin.manager.plugin.Plugin;
import com.jayfella.plugin.manager.plugin.description.PluginDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimplePluginLoader implements PluginLoader {

    private static final String TYPE = "SimplePlugin";

    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final List<SimplePluginClassLoader> loaders = new ArrayList<>();

    public SimplePluginLoader() {

    }

    @Override
    @NotNull
    public String getTypeName() {
        return TYPE;
    }

    @Override
    public Plugin loadPlugin(@NotNull PluginManager pluginManager, @NotNull File pluginFile, @NotNull PluginDescription description) throws InvalidPluginException {

        final File parentFile = pluginFile.getParentFile();
        final File dataFolder = new File(parentFile, description.getId());

        if (dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new InvalidPluginException(String.format(
                    "Projected datafolder: `%s' for %s (%s) exists and is not a directory",
                    dataFolder,
                    description.getFullName(),
                    pluginFile
            ));
        }

        final SimplePluginClassLoader classLoader;

        try {
            classLoader = new SimplePluginClassLoader(pluginManager, this, pluginFile, description, dataFolder);
        } catch (IOException e) {
            throw new InvalidPluginException(e);
        }

        loaders.add(classLoader);
        return classLoader.getPlugin();
    }

    @Nullable
    Class<?> getClassByName(final String name) {
        Class<?> cachedClass = classes.get(name);

        if (cachedClass != null) {
            return cachedClass;
        } else {
            for (SimplePluginClassLoader loader : loaders) {
                try {
                    cachedClass = loader.findClass(name, false);
                } catch (ClassNotFoundException cnfe) {}
                if (cachedClass != null) {
                    return cachedClass;
                }
            }
        }
        return null;
    }

    void setClass(@NotNull final String name, @NotNull final Class<?> clazz) {
        if (!classes.containsKey(name)) {
            classes.put(name, clazz);

            /*
            if (ConfigurationSerializable.class.isAssignableFrom(clazz)) {
                Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass(ConfigurationSerializable.class);
                ConfigurationSerialization.registerClass(serializable);
            }
             */

        }
    }

    @Override
    public @Nullable ClassLoader getClassLoader(@NotNull Plugin plugin) {
        return loaders.stream()
                .filter( loader -> loader.getPlugin() == plugin )
                .findFirst()
                .orElse(null);
    }



}
