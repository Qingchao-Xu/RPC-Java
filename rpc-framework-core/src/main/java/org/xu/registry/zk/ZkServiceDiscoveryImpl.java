package org.xu.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.xu.enums.LoadBalanceEnum;
import org.xu.enums.RpcErrorMessageEnum;
import org.xu.exception.RpcException;
import org.xu.extension.ExtensionLoader;
import org.xu.loadbalance.LoadBalance;
import org.xu.registry.ServiceDiscovery;
import org.xu.registry.zk.util.CuratorUtils;
import org.xu.remoting.dto.RpcRequest;
import org.xu.utils.CollectionUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * 服务发现（基于zookeeper实现）
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.CONSISTENT_HASH.getName());
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // 负载均衡
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
