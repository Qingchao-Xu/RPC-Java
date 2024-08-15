package org.xu.registry;

import org.xu.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册
 */
@SPI
public interface ServiceRegistry {
    /**
     * 注册服务到服务中心
     * @param rpcServiceName 完整的服务名称（class name + group + version）
     * @param inetSocketAddress 远程服务地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
