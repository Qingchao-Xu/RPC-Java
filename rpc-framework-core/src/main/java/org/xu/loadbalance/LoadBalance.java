package org.xu.loadbalance;

import org.xu.extension.SPI;
import org.xu.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 负载均衡策略的接口
 */
@SPI
public interface LoadBalance {
    /**
     * 从服务地址列表中选择一个
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
