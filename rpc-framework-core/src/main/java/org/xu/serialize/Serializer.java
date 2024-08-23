package org.xu.serialize;

import org.xu.extension.SPI;

/**
 * 序列化接口
 */
@SPI
public interface Serializer {
    /**
     * 序列化
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
