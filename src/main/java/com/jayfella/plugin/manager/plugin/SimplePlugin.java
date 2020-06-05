package com.jayfella.plugin.manager.plugin;

import com.jayfella.plugin.manager.SimplePluginClassLoader;
import com.jayfella.plugin.manager.SimplePluginLoader;
import com.jayfella.plugin.manager.SimplePluginManager;
import com.jayfella.plugin.manager.PluginLoader;
import com.jayfella.plugin.manager.PluginManager;
import com.jayfella.plugin.manager.plugin.description.PluginDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Logger;

public abstract class SimplePlugin extends BasePlugin {

    private SimplePluginManager pluginManager;
    private SimplePluginLoader pluginLoader;
    private SimplePluginClassLoader classLoader;
    private PluginDescription description;
    private File file;
    private File dataFolder;

    private PluginLogger logger;

    private boolean enabled = false;

    @Override
    public void initialize(@NotNull PluginManager pluginManager, @NotNull PluginLoader pluginLoader, @NotNull SimplePluginClassLoader pluginClassLoader, @NotNull PluginDescription description, @NotNull File file, @Nullable File dataFolder) {

        this.pluginManager = (SimplePluginManager) pluginManager;
        this.pluginLoader = (SimplePluginLoader) pluginLoader;
        this.classLoader = pluginClassLoader;
        this.description = description;
        this.file = file;
        this.dataFolder = dataFolder;

        this.logger = new PluginLogger(this);
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    @Override
    @NotNull
    public PluginDescription getDescription() { return description; }

    @Override
    @NotNull
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    @NotNull
    public SimplePluginLoader getPluginLoader() {
        return pluginLoader;
    }

    @Override
    @NotNull
    public SimplePluginClassLoader getPluginClassLoader() {
        return classLoader;
    }

    @Override
    @NotNull
    public File getFile() {
        return file;
    }

    @Override
    @Nullable
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public Plugin[] getDependencies() {

        Plugin[] dependencies = new Plugin[description.getDependencies().size()];

        for (int i = 0; i < dependencies.length; i++) {
            Plugin plugin = pluginManager.getPlugin(description.getDependencies().get(i));
            dependencies[i] = plugin;
        }

        return dependencies;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
