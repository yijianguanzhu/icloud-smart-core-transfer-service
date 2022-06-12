package com.yijianguanzhu.transfer.boot.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author yijianguanzhu 2022年06月10日
 */
@Configuration
@EnableFeignClients("com.yijianguanzhu.transfer.client")
public class FeignAutoConfiguration {
}
