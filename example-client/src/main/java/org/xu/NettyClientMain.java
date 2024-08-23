package org.xu;

import org.xu.enums.RpcRequestTransportEnum;
import org.xu.extension.ExtensionLoader;
import org.xu.proxy.RpcClientProxy;
import org.xu.remoting.transport.RpcRequestTransport;

/**
 * 使用Netty传输的rpc客户端
 */
public class NettyClientMain {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = ExtensionLoader
                .getExtensionLoader(RpcRequestTransport.class)
                .getExtension(RpcRequestTransportEnum.NETTY.getName());
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.sayHello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
