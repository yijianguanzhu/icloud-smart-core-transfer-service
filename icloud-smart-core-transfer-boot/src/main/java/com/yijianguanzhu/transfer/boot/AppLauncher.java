package com.yijianguanzhu.transfer.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yijianguanzhu 2022年05月16日
 */
@SpringBootApplication(exclude = { RabbitAutoConfiguration.class })
@ComponentScan("com.yijianguanzhu.transfer")
public class AppLauncher {

	public static void main( String[] args ) {
		SpringApplication.run( AppLauncher.class, args );
	}
}
