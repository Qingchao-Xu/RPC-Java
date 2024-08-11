package org.xu.remoting.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * rpc请求实体类
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId; // 请求id
    private String interfaceName; // 接口名称
    private String methodName; // 方法名称
    private Object[] parameters; // 参数
    private Class<?>[] paramTypes; // 参数类型
    private String version;
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
