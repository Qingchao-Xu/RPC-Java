package org.xu.remoting.constants;

/**
 * 常量
 */
public class RpcConstants {
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'}; // 魔法数，用来验证RpcMessage
    public static final byte VERSION = 1; // 版本号
    public static final byte TOTAL_LENGTH = 16; // 可读数据的最小长度
    public static final int HEAD_LENGTH = 16; // 消息头长度
    public static final byte REQUEST_TYPE = 1; // 消息类型为请求
    public static final byte RESPONSE_TYPE = 2; // 消息类型为响应
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024; // netty自定义消息的最大长度
}
