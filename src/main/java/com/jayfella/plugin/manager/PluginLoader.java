package com.jayfella.plugin.manager;

import com.jayfella.plugin.manager.exception.InvalidPluginException;
import com.jayfella.plugin.manager.plugin.Plugin;
import com.jayfella.plugin.manager.plugin.description.PluginDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface PluginLoader {

    @NotNull
    String getTypeName();

    @Nullable
    Plugin loadPlugin(@NotNull PluginManager pluginManager, @NotNull File file, @NotNull PluginDescription description) throws InvalidPluginException;

    @Nullable
    ClassLoader getClassLoader(@NotNull Plugin plugin);

}
