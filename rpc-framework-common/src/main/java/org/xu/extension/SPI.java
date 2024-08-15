package org.xu.extension;

import java.lang.annotation.*;

/**
 * SPI注解，被注解声明的接口说明可以加载扩展类，通过仿Dubbo扩展类加载器
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
