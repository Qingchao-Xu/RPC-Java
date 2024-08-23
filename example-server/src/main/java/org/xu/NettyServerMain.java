package org.xu;

import org.xu.config.RpcServiceConfig;
import org.xu.remoting.transport.netty.server.NettyRpcServer;
import org.xu.serviceimpl.HelloServiceImpl2;

/**
 * 使用Netty做网络传输的rpc服务端
 */
public class NettyServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl2();
        NettyRpcServer nettyRpcServer = new NettyRpcServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
