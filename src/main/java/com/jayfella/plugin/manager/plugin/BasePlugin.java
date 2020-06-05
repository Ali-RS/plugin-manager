package com.jayfella.plugin.manager.plugin;

import org.jetbrains.annotations.NotNull;

public abstract class BasePlugin implements Plugin {

    public BasePlugin() {

    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Plugin)) {
            return false;
        }
        return getId().equals(((Plugin) obj).getId());
    }

    @Override
    public final @NotNull String getId() {
        return getDescription().getId();
    }

}
