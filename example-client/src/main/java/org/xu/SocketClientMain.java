package org.xu;

import org.xu.proxy.RpcClientProxy;
import org.xu.remoting.dto.RpcRequest;
import org.xu.remoting.dto.RpcResponse;
import org.xu.remoting.transport.RpcRequestTransport;
import org.xu.remoting.transport.socket.SocketRpcClient;

import java.util.UUID;

public class SocketClientMain {
    public static void main(String[] args) {

        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String result = helloService.sayHello(new Hello("111", "222"));
        System.out.println(result);
    }
}
