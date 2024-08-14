package org.xu.registry.zk.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.xu.enums.RpcConfigEnum;
import org.xu.utils.PropertiesFileUtil;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Curator 工具类
 */
@Slf4j
public final class CuratorUtils {

    private static final int BASE_SLEEP_TIME = 1000; // 初始重试时间
    private static final int MAX_RETRIES = 3; // 重试次数
    public static final String ZK_REGISTER_ROOT_PATH = "/java-rpc"; // 服务注册的 zk 根路径
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>(); // 已经注册的服务名和地址
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet(); // 已经注册创建的路径
    private static CuratorFramework zkClient; // zk客户端
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181"; // 默认zk服务器的地址

    private CuratorUtils() { // 工具类，私有构造方法
    }

    /**
     * 获取zk客户端
     *
     * @return CuratorFramework
     */
    public static CuratorFramework getZkClient() {
        // 检查用户是否设置了 zk 地址
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress =
                properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ?
                        properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;
        // 如果 zk 客户端已经启动，直接返回
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // 重试策略，重试3次，每次会增大重试时间
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress) // 要连接的服务器地址，可以是列表
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // 等待30s，或者连接上zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return zkClient;
    }

    /**
     * 创建持久节点。 临时节点更合适，为什么用持久节点呢？
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                // /java-rpc/org.xu.HelloService/127.0.0.1:8888
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    /**
     * 获取节点的子节点
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            // 注册监听器，监听子节点的变化修改map缓存
            registerWatcher(zkClient, rpcServiceName);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * 注册监听器，监听指定节点
     */
    private static void registerWatcher(CuratorFramework zkClient, String rpcServiceName) {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        CuratorCache curatorCache = CuratorCache.builder(zkClient, servicePath).build();
        CuratorCacheListener cacheListener = CuratorCacheListener.builder().forPathChildrenCache(servicePath, zkClient, (client, event) -> {
            List<String> serviceAddress = client.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddress);
        }).build();
        curatorCache.listenable().addListener(cacheListener);
        curatorCache.start();
        // 不能关闭
    }

}
