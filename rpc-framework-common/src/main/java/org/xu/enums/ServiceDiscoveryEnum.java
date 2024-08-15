package org.xu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务发现实现方式
 */
@AllArgsConstructor
@Getter
public enum ServiceDiscoveryEnum {
    ZK("zk");
    private final String name;
}
