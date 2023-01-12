package com.yijianguanzhu.transfer.cluster.stream.handler;

import com.yijianguanzhu.transfer.cluster.enums.ClusterCmdEnum;
import com.yijianguanzhu.transfer.cluster.message.ClusterMessage;
import com.yijianguanzhu.transfer.cluster.stream.binder.NodeChannel;
import com.yijianguanzhu.transfer.common.props.TransferProperties;
import com.yijianguanzhu.transfer.server.connector.AbstractConnector;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author yijianguanzhu 2023年01月12日
 */
@Component
@Slf4j
class Consumer {

	@Autowired
	private TransferProperties transferProperties;

	@Autowired
	private AbstractConnector abstractConnector;

	@StreamListener(NodeChannel.INPUT)
	public void receiveMessage( Message<ClusterMessage> message ) {
		log.info( "cluster message:{}", message );
		ClusterMessage payload = message.getPayload();
		if ( !Objects.equals( transferProperties.getCluster().getInstanceId(), payload.getInstanceId() ) ) {
			String sessionId = String.valueOf( payload.getUserId() );
			if ( payload.getClusterCmdEnum() == ClusterCmdEnum.NOTICE ) {
				abstractConnector.send( sessionId, AppMessage.builder()
						.cmd( CmdEnum.NOTICE ).code( CodeEnum.SUCCESS )
						.msg( payload.getMsg() ).build() );
			}
			if ( payload.getClusterCmdEnum() == ClusterCmdEnum.OFF ) {
				abstractConnector.close( sessionId );
			}
		}
	}
}
