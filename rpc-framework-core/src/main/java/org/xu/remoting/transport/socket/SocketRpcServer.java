package org.xu.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
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

    private final Object service;
    private final ExecutorService threadPool;

    public SocketRpcServer(Object service) {
        this.service = service;
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
    }

    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket, service));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }
}
