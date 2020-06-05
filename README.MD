Plugin Manager
===

An implementation of a plugin framework.

```java
PluginManager pluginManager = new SimplePluginManager();
```

#### Creating a Plugin

```java
public class TestPlugin extends SimplePlugin {
        
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
```