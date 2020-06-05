package com.jayfella.plugin.manager.plugin;

import com.jayfella.plugin.manager.PluginLoader;
import com.jayfella.plugin.manager.PluginManager;
import com.jayfella.plugin.manager.SimplePluginClassLoader;
import com.jayfella.plugin.manager.plugin.description.PluginDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Logger;

public interface Plugin {

    void initialize(@NotNull PluginManager pluginManager, @NotNull PluginLoader pluginLoader, @NotNull SimplePluginClassLoader pluginClassLoader, @NotNull PluginDescription description, @NotNull File file, @Nullable File dataFolder);

    @NotNull
    Logger getLogger();

    @NotNull
    PluginDescription getDescription();

    @NotNull
    PluginManager getPluginManager();

    @NotNull
    PluginLoader getPluginLoader();

    @NotNull
    SimplePluginClassLoader getPluginClassLoader();

    @NotNull
    File getFile();

    @Nullable
    File getDataFolder();

    @NotNull
    Plugin[] getDependencies();

    @NotNull
    String getId();

    boolean isEnabled();
    void setEnabled(boolean enabled);

    void onLoad();
    void onUnload();

    void onEnable();
    void onDisable();

}
