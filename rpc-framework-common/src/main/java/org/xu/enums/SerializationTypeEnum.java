package org.xu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 序列化类型
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {

    KRYO((byte) 0x01, "kryo"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    HESSIAN((byte) 0x03, "hessian");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
