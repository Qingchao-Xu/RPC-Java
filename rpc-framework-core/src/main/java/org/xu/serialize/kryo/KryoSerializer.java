package org.xu.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.xu.exception.SerializeException;
import org.xu.remoting.dto.RpcRequest;
import org.xu.remoting.dto.RpcResponse;
import org.xu.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Kryo序列化
 */
@Slf4j
public class KryoSerializer implements Serializer {

    /**
     * 由于 Kryo 不是线程安全的，所以需要使用 ThreadLocal 存储
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // 将对象转换成byte数组

            kryo.writeObject(output, obj);

            output.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Serialization failed", e);
            throw new SerializeException("Serialization failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // 从byte数组反序列化为对象
            return kryo.readObject(input, clazz);
        } catch (IOException e) {
            log.error("Deserialization failed", e);
            throw new SerializeException("Deserialization failed", e);
        }
    }
}
