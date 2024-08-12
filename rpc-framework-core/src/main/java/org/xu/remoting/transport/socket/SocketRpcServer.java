package org.xu.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import org.xu.remoting.dto.RpcRequest;
import org.xu.remoting.dto.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

/**
 * socket 实现的 RPC 服务端
 */
@Slf4j
public class SocketRpcServer {
    public static final int PORT = 9998;

    private Object service;

    public SocketRpcServer(Object service) {
        this.service = service;
    }

    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                     ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
                    RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
                    // 反射调用方法
                    Object result;
                    try {
                        System.out.println(Arrays.toString(rpcRequest.getParamTypes()));
                        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
                        result = method.invoke(service, rpcRequest.getParameters());
                        log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
                    } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                    objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
                    objectOutputStream.flush();
                } catch (ClassNotFoundException e) {
                    log.error("occur exception:", e);
                }
            }
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }
}
