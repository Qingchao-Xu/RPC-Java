package org.xu.remoting.dto;

import lombok.*;
import org.xu.enums.RpcResponseCodeEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * rpc响应实体类
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 715745410605631233L;
    private String requestId; // 请求id
    /**
     * response code 响应状态码
     */
    private Integer code;
    /**
     * response message 响应消息
     */
    private String message;
    /**
     * response body 响应数据
     */
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }
}
