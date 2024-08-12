package org.xu.serviceimpl;

import lombok.extern.slf4j.Slf4j;
import org.xu.Hello;
import org.xu.HelloService;

/**
 * api 的具体实现
 */
@Slf4j
public class HelloServiceImpl2 implements HelloService {

    static {
        System.out.println("HelloServiceImpl2被创建");
    }

    @Override
    public String sayHello(Hello hello) {
        log.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回2: {}.", result);
        return result;
    }
}
