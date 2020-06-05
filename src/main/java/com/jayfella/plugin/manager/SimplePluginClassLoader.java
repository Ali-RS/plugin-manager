package com.jayfella.plugin.manager;

import com.google.common.io.ByteStreams;
import com.jayfella.plugin.manager.exception.InvalidPluginException;
import com.jayfella.plugin.manager.plugin.Plugin;
import com.jayfella.plugin.manager.plugin.SimplePlugin;
import com.jayfella.plugin.manager.plugin.description.PluginDescription;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

public class SimplePluginClassLoader extends URLClassLoader {

    private final SimplePluginLoader loader;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final PluginDescription description;
    private final PluginManager pluginManager;

    private final JarFile jar;
    private final Manifest manifest;
    private final URL url;

    private final Plugin plugin;

    private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        ClassLoader.registerAsParallelCapable();
    }

    SimplePluginClassLoader(@NotNull PluginManager pluginManager, @NotNull final SimplePluginLoader loader, @NotNull final File file, @NotNull final PluginDescription description, File dataFolder) throws IOException, InvalidPluginException {
        super(new URL[] { file.toURI().toURL() },  loader.getClass().getClassLoader());

        this.pluginManager = pluginManager;
        this.loader = loader;
        this.description = description;

        this.jar = new JarFile(file);
        this.manifest = jar.getManifest();
        this.url = file.toURI().toURL();

        Class<?> jarClass;

        try {
            jarClass = Class.forName(description.getMain(), true, this);

        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new InvalidPluginException("Cannot find the specified main class '" + description.getMain() + "'", e);
        }

        Class<? extends SimplePlugin> pluginClass;

        try {
            pluginClass = jarClass.asSubclass(SimplePlugin.class);
        } catch (ClassCastException | NoClassDefFoundError e) {
            throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend SimplePlugin", e);
        }

        try {
            Constructor<? extends SimplePlugin> pluginConstructor = pluginClass.getConstructor();
            plugin = pluginConstructor.newInstance();

            plugin.initialize(pluginManager, loader, this, description, file, dataFolder);

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InvalidPluginException("No public constructor.", e);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new InvalidPluginException("Abnormal plugin type.", e);
        }
    }

    @Override
    public URL getResource(String name) {
        return findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return findResources(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    Class<?> findClass(@NotNull String name, boolean checkGlobal) throws ClassNotFoundException {

        Class<?> result = classes.get(name);

        if (result == null) {
            if (checkGlobal) {
                result = loader.getClassByName(name);

                if (result != null) {
                    PluginDescription provider = ((SimplePluginClassLoader) result.getClassLoader()).description;

                    if (provider != description
                            && !seenIllegalAccess.contains(provider.getId())
                            && !pluginManager.isTransitiveDependency(description, provider)) {

                        seenIllegalAccess.add(provider.getId());
                        if (plugin != null) {
                            plugin.getLogger().log(Level.WARNING, "Loaded class {0} from {1} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{name, provider.getFullName()});
                        } else {
                            // In case the bad access occurs on construction
                            pluginManager.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getId(), name, provider.getFullName()});
                        }
                    }
                }
            }

            if (result == null) {
                String path = name.replace('.', '/').concat(".class");
                JarEntry entry = jar.getJarEntry(path);

                if (entry != null) {
                    byte[] classBytes;

                    try (InputStream is = jar.getInputStream(entry)) {
                        classBytes = ByteStreams.toByteArray(is);
                    } catch (IOException ex) {
                        throw new ClassNotFoundException(name, ex);
                    }

                    classBytes = pluginManager.getClassSerializer().processClass(description, path, classBytes);

                    int dot = name.lastIndexOf('.');
                    if (dot != -1) {
                        String pkgName = name.substring(0, dot);

                        //noinspection deprecation
                        if (getPackage(pkgName) == null) {
                            try {
                                if (manifest != null) {
                                    definePackage(pkgName, manifest, url);
                                } else {
                                    definePackage(pkgName, null, null, null, null, null, null, null);
                                }
                            } catch (IllegalArgumentException ex) {
                                //noinspection deprecation
                                if (getPackage(pkgName) == null) {
                                    throw new IllegalStateException("Cannot find package " + pkgName);
                                }
                            }
                        }
                    }

                    CodeSigner[] signers = entry.getCodeSigners();
                    CodeSource source = new CodeSource(url, signers);

                    result = defineClass(name, classBytes, 0, classBytes.length, source);
                }

                if (result == null) {
                    result = super.findClass(name);
                }

                if (result != null) {
                    loader.setClass(name, result);
                }

                classes.put(name, result);
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            jar.close();
        }
    }

    @NotNull
    Set<String> getClasses() {
        return classes.keySet();
    }

    public Plugin getPlugin() {
        return plugin;
    }

}
