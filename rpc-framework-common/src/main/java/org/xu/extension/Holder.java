package org.xu.extension;

/**
 * 封装扩展类实例，还有一个作用：封装接口的所有扩展类类型
 */
public class Holder<T> {
    private volatile T value;
    public T get() {
        return value;
    }
    public void set(T value) {
        this.value = value;
    }
}
