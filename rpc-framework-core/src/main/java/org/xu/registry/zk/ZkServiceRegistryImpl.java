package org.xu.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.xu.registry.ServiceRegistry;
import org.xu.registry.zk.util.CuratorUtils;

import java.net.InetSocketAddress;

/**
 * 服务注册（基于Zookeeper实现）
 */
@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        // inetSocketAddress.toString()的到的形式为  /ip:port   所以不用加 /
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
