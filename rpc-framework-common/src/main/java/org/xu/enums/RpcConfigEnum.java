package org.xu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * rpc配置相关
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
