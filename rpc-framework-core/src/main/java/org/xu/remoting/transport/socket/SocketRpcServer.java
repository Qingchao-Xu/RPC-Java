package org.xu.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import org.xu.config.RpcServiceConfig;
import org.xu.factory.SingletonFactory;
import org.xu.provider.ServiceProvider;
import org.xu.provider.impl.ZkServiceProviderImpl;
import org.xu.remoting.dto.RpcRequest;
import org.xu.remoting.dto.RpcResponse;
import org.xu.utils.concurrent.threadpool.ThreadPoolFactoryUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * socket 实现的 RPC 服务端
 */
@Slf4j
public class SocketRpcServer {
    public static final int PORT = 9998;

    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;


    public SocketRpcServer() {
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket, serviceProvider));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }
}
