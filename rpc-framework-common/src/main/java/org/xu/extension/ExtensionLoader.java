package org.xu.extension;

import lombok.extern.slf4j.Slf4j;
import org.xu.utils.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模仿 Dubbo 的扩展类加载机制 ： https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/reference-manual/spi/description/dubbo-spi/
 */
@Slf4j
public final class ExtensionLoader<T> {
    private static final String SERVICE_DIRECTORY = "META_INF/extensions/"; // 放置扩展类配置文件的路径
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>(); // 缓存扩展类加载器
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>(); // 缓存所有接口的所有扩展类的实例对象

    private final Class<?> type; // 扩展类加载器对应的类型
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>(); // 扩展类加载器缓存的扩展类实例
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>(); // 扩展类加载器缓存的所有扩展类类型

    private ExtensionLoader(Class<?> type) { // 私有构造方法，只能通过静态方法获取
        this.type = type;
    }

    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) { // 静态方法，获取扩展类加载器
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        // 首先从缓存里获取扩展类加载器，如果缓存中没有，就创建一个新的
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    public T getExtension(String name) { // 获取具体扩展类的实例
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        // 先从缓存中获取具体扩展类实例，如果没有，就创建新的
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // 如果holder里没有封装扩展类实例，就创建一个
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) { // 双重锁校验，保证安全
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    private T createExtension(String name) { // 创建具体扩展类的实例对象
        // 加载扩展接口 T 的所有扩展类，并根据名称找到对应的
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name); // 找不到对应名称的扩展类实现
        }
        // 先从类的缓存中取，取不到再通过反射创建
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.getDeclaredConstructor().newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses() { // 获取接口 T 的所有扩展类类型
        // 从缓存中获取 T 的所有扩展类
        Map<String, Class<?>> classes = cachedClasses.get();
        // 双重锁校验
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // 加载T 所有的扩展类，从配置的目录文件中
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses) { // 从配置的文件中加载扩展类类型
        // 路径名和接口的全限定类名拼接为文件路径
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName); // 根据文件路径获取文件，（原Dubbo这里有多个配置目录，所以可能有多个配置文件，所以用urls）
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    // 对文件中的内容逐条进行加载
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) { // 对文件中的内容逐条进行加载
        // 创建流
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), StandardCharsets.UTF_8))){
            String line;
            // 读取每一行
            while ((line = reader.readLine()) != null) {
                // 本行注解标识符所在的位置
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // 忽略掉注解
                    line = line.substring(0, ci);
                }
                line = line.trim(); // 去掉头尾空格
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('='); // 得到 = 所在位置
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        // SPI 采用 k-v 的形式存储，所以两个都不能为空
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
