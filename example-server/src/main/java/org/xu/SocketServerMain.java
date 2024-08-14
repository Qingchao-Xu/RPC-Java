package org.xu;

import org.xu.config.RpcServiceConfig;
import org.xu.remoting.transport.socket.SocketRpcServer;
import org.xu.serviceimpl.HelloServiceImpl;

public class SocketServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
