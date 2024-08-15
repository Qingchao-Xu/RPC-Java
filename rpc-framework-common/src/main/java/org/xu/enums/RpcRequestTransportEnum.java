package org.xu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发送rpc请求的实现方式
 */
@AllArgsConstructor
@Getter
public enum RpcRequestTransportEnum {

    SOCKET("socket");

    private final String name;
}
