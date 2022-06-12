package com.yijianguanzhu.transfer.cluster.rabbit;

import com.yijianguanzhu.transfer.cluster.enums.ClusterCmdEnum;
import com.yijianguanzhu.transfer.cluster.message.ClusterMessage;
import com.yijianguanzhu.transfer.common.props.TransferProperties;
import com.yijianguanzhu.transfer.server.connector.AbstractConnector;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author yijianguanzhu 2022年06月02日
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "transfer-service.cluster.rabbit.enable", havingValue = "true")
class RabbitConsumer {

	@Autowired
	private TransferProperties transferProperties;

	@Autowired
	private AbstractConnector abstractConnector;

	@Autowired
	private Queue queue;

	@Autowired
	private MessageConverter messageConverter;

	// 注册消费者
	@Bean
	public SimpleMessageListenerContainer simpleMessageListenerContainer( ConnectionFactory connectionFactory ) {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer( connectionFactory );
		listenerContainer.setQueueNames( queue.getName() );
		listenerContainer.setMessageListener( this::receiveMessage );
		return listenerContainer;
	}

	public void receiveMessage( Message message ) {
		receiveMessage( ( ClusterMessage ) messageConverter.fromMessage( message ) );
	}

	public void receiveMessage( ClusterMessage message ) {
		log.info( "cluster message:{}", message );
		if ( !Objects.equals( transferProperties.getCluster().getInstanceId(), message.getInstanceId() ) ) {
			String sessionId = String.valueOf( message.getUserId() );
			if ( message.getClusterCmdEnum() == ClusterCmdEnum.NOTICE ) {
				abstractConnector.send( sessionId, AppMessage.builder()
						.cmd( CmdEnum.NOTICE ).code( CodeEnum.SUCCESS )
						.msg( message.getMsg() ).build() );
			}
			if ( message.getClusterCmdEnum() == ClusterCmdEnum.OFF ) {
				abstractConnector.close( sessionId );
			}
		}
	}
}
