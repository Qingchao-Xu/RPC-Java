package org.xu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 负载均衡的实现方式
 */
@AllArgsConstructor
@Getter
public enum LoadBalanceEnum {

    RANDOM("random"),
    CONSISTENT_HASH("consistentHash");

    private final String name;
}
