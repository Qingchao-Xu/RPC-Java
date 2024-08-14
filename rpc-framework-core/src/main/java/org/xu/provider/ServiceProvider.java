package org.xu.provider;

import org.xu.config.RpcServiceConfig;

/**
 * 存储和提供服务对象
 */
public interface ServiceProvider {
    void addService(RpcServiceConfig rpcServiceConfig); // 存储服务对象
    Object getService(String rpcServiceName); // 根据rpcServiceName获取服务对象
    void publishService(RpcServiceConfig rpcServiceConfig); // 发布服务（服务注册 + 存储服务对象）
}
