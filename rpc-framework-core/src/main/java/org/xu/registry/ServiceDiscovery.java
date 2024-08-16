package org.xu.registry;

import org.xu.extension.SPI;
import org.xu.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现
 */
@SPI
public interface ServiceDiscovery {
    /**
     * 根据 rpcServiceName 获取远程服务地址
     * @param rpcRequest 完整的服务名称（class name + group + version）
     * @return 远程服务地址
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
