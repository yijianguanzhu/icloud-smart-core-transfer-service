package com.yijianguanzhu.transfer.cluster.rabbit;

import com.yijianguanzhu.transfer.common.message.cluster.ProducerProxy;
import com.yijianguanzhu.transfer.common.props.TransferProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 使用direct直连交换机通信模型，消息只会被投入到routingKey一致的队列中
 *
 * @author yijianguanzhu 2022年06月02日
 * @see com.yijianguanzhu.transfer.cluster.stream.config.StreamAutoConfiguration
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@Deprecated
@ConditionalOnProperty(name = "transfer-service.cluster.rabbit.enable", havingValue = "true")
@Import(RabbitAutoConfiguration.class)
class RabbitProducer implements ProducerProxy {

	@Autowired
	private RabbitProperties rabbitProperties;

	@Autowired
	private TransferProperties transferProperties;

	@Bean
	DirectExchange directExchange() {
		return new DirectExchange( rabbitProperties.getTemplate().getExchange(), true, false );
	}

	@Bean
	public Queue queue() {
		String queue = rabbitProperties.getTemplate().getDefaultReceiveQueue() + "_"
				+ transferProperties.getCluster().getInstanceId();
		// 消费者下线时，示意服务端自动删除队列
		return new Queue( queue, true, false, true );
	}

	@Bean
	Binding exchangeBinding( DirectExchange directExchange, Queue queue ) {
		return BindingBuilder.bind( queue ).to( directExchange )
				.with( rabbitProperties.getTemplate().getRoutingKey() );
	}

	@Bean
	MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public boolean produce( Object clusterMessage ) {
		rabbitTemplate.convertAndSend( clusterMessage );
		return true;
	}
}
