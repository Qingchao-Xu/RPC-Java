package org.xu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务注册的实现方式
 */
@AllArgsConstructor
@Getter
public enum ServiceRegistryEnum {

    ZK("zk");

    private final String name;
}
