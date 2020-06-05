package com.jayfella.plugin.manager;

import com.jayfella.plugin.manager.exception.InvalidPluginDescriptionException;
import com.jayfella.plugin.manager.exception.InvalidPluginException;
import com.jayfella.plugin.manager.plugin.Plugin;
import com.jayfella.plugin.manager.plugin.description.PluginDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Logger;

public interface PluginManager {

    void registerInterface(@NotNull Class<? extends PluginLoader> loader);

    @NotNull Logger getLogger();

    @Nullable Plugin loadPlugin(File file) throws InvalidPluginException, InvalidPluginDescriptionException;
    @NotNull Plugin[] loadPlugins(File directory);
    void enablePlugins();

    @Nullable Plugin getPlugin(String name);
    @NotNull Plugin[] getPlugins();

    long getLoadedPluginCount();
    long getEnabledPluginCount();
    long getDisabledPluginCount();
    long getPluginsNotLoadedCount();

    ClassSerializer getClassSerializer();

    boolean isTransitiveDependency(@NotNull PluginDescription plugin, @NotNull PluginDescription depend);
}
