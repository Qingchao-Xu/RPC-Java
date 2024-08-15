package org.xu.remoting.transport;

import org.xu.extension.SPI;
import org.xu.remoting.dto.RpcRequest;

/**
 * 发送rpc请求的接口，方便用不同的方式实现网络传输
 */
@SPI
public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
