package org.xu;

import org.xu.config.RpcServiceConfig;
import org.xu.proxy.RpcClientProxy;
import org.xu.remoting.transport.RpcRequestTransport;
import org.xu.remoting.transport.socket.SocketRpcClient;

public class SocketClientMain {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceConfig);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String result = helloService.sayHello(new Hello("111", "222"));
        System.out.println(result);
    }
}
