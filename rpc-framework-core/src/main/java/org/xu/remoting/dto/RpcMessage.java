package org.xu.remoting.dto;

import lombok.*;
import org.checkerframework.checker.units.qual.N;

/**
 * netty 传输时自定义的消息格式
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    private byte messageType; // 消息类型
    private byte codec; // 序列化类型
    private byte compress; // 压缩类型
    private int requestId; // 请求id
    private Object data; // 数据
}
