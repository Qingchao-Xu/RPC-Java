package org.xu.exception;

import org.xu.enums.RpcErrorMessageEnum;

/**
 * 自定义 rpc 异常
 */
public class RpcException extends RuntimeException {
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
