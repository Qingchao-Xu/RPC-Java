package org.xu.loadbalance;

import org.xu.remoting.dto.RpcRequest;
import org.xu.utils.CollectionUtil;

import java.util.List;

/**
 * 负载均衡策略的抽象类，提取公共代码
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            return null;
        }
        if (serviceUrlList.size() == 1) {
            return serviceUrlList.getFirst();
        }
        return doSelect(serviceUrlList, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);
}
