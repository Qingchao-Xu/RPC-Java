package org.xu.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import org.xu.remoting.dto.RpcRequest;
import org.xu.remoting.dto.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * socket 服务端线程池
 */
@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable{

    private final Socket socket;
    private final Object service;

    public SocketRpcRequestHandlerRunnable(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            // 反射调用方法
            Object result;
            try {
                Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
                result = method.invoke(service, rpcRequest.getParameters());
                log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (ClassNotFoundException | IOException e) {
            log.error("occur exception:", e);
        }
    }
}
