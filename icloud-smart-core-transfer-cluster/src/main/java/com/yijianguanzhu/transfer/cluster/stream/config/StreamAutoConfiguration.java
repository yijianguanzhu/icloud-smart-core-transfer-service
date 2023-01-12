package com.yijianguanzhu.transfer.cluster.stream.config;

import com.yijianguanzhu.transfer.cluster.stream.binder.NodeChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.config.BindingServiceConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * spring cloud stream  配置
 *
 * @author yijianguanzhu 2023年01月12日
 */
@Slf4j
@Configuration
@AutoConfigureBefore({ BindingServiceConfiguration.class })
@EnableBinding(NodeChannel.class)
public class StreamAutoConfiguration {
}
