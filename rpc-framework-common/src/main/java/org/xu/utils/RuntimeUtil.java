package org.xu.utils;

/**
 * 运行时环境的工具类
 */
public class RuntimeUtil {

    /**
     * 获取cpu核心数
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
