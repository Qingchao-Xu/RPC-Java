package org.xu.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.xu.enums.RpcErrorMessageEnum;
import org.xu.exception.RpcException;
import org.xu.registry.ServiceDiscovery;
import org.xu.registry.zk.util.CuratorUtils;
import org.xu.utils.CollectionUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * 服务发现（基于zookeeper实现）
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // 负载均衡，随机,后续更新补充
        Random random = new Random();
        String targetServiceUrl = serviceUrlList.get(random.nextInt(serviceUrlList.size()));
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
