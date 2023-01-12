package com.yijianguanzhu.transfer.cluster.stream.handler;

import com.yijianguanzhu.transfer.cluster.stream.binder.NodeChannel;
import com.yijianguanzhu.transfer.common.message.cluster.ProducerProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author yijianguanzhu 2023年01月12日
 */
@Component
class Producer implements ProducerProxy {

	@Autowired
	private NodeChannel nodeChannel;

	@Override
	public boolean produce( Object clusterMessage ) {
		return nodeChannel.output().send( MessageBuilder.withPayload( clusterMessage ).build() );
	}
}
