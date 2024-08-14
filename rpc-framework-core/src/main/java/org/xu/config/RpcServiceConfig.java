package org.xu.config;

import lombok.*;

/**
 * rpc服务的配置信息
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    private String version = ""; // 服务版本
    private String group = ""; // 当接口有多个实现类时，按组进行区分
    private Object service = ""; // 目标服务对象


    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }
}
