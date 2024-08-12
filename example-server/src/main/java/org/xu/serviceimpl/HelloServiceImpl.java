package org.xu.serviceimpl;

import lombok.extern.slf4j.Slf4j;
import org.xu.Hello;
import org.xu.HelloService;

/**
 * api 的具体实现
 */
@Slf4j
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String sayHello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}
