package org.xu.loadbalance.loadbalancer;

import org.xu.loadbalance.AbstractLoadBalance;
import org.xu.remoting.dto.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 仿Dubbo实现一致性哈希负载均衡：
 * https://cn.dubbo.apache.org/zh-cn/blog/2019/05/01/dubbo-%e4%b8%80%e8%87%b4%e6%80%a7hash%e8%b4%9f%e8%bd%bd%e5%9d%87%e8%a1%a1%e5%ae%9e%e7%8e%b0%e5%89%96%e6%9e%90/
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(serviceAddresses); // 可以用来识别地址列表是否发生过变更
        String rpcServiceName = rpcRequest.getRpcServiceName(); // 完整的 rpcServiceName 来取出选择器
        ConsistentHashSelector selector = selectors.get(rpcServiceName); // 取出对应的选择器
        // 如果选择器不存在，或者地址列表被更新过,就创建一个新的
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        // 根据rpc请求的服务名和参数列表，计算hash值，得到对应的服务地址
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    private static final class ConsistentHashSelector { // 选择器，用来做映射关系的
        private final TreeMap<Long, String> virtualInvokers; // 存储 hash值 与 地址映射关系的TreeMap
        private final int replicaNumber = 160; // 副本数目（虚拟节点的数目），这里默认为160个
        private final int identityHashCode; // hash码，用来识别地址列表是否发生过变更

        ConsistentHashSelector(List<String> invokers, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            // 遍历所有的服务地址
            for (String invoker : invokers) {
                // 创建该地址所有虚拟节点的 hash 值，并存到 TreeMap中
                for (int i = 0; i < replicaNumber / 4; i++) { // 先获取 160 / 4 个虚拟节点的 md5 码
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) { // 对每个md5码再进行4次位数级别的散列，（加强散列效果？）
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        private static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return md.digest();
        }

        private static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24
                    | (long) (digest[2 + idx * 4] & 255) << 16
                    | (long) (digest[1 + idx * 4] & 255) << 8
                    | (long) (digest[idx * 4] & 255))
                    & 4294967295L;
        }

        public String select(String rpcServiceKey) {
            byte[] bytes = md5(rpcServiceKey);
            return selectFactory(hash(bytes, 0)); // 根据hash值取出对应的服务地址
        }

        public String selectFactory(long hashCode) { // 根据hash值取出对应的服务地址
            // 根据hashCode，获取第一个哈希值大于等于 hashCode 的元素
            Map.Entry<Long, String> entry = virtualInvokers.ceilingEntry(hashCode);
            if (entry == null) {
                // 如果没有获取到，说明没有比 hashCode 更大的，就直接用第一个元素
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }



    }
}
