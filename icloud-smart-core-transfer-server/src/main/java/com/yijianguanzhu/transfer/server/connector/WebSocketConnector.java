package com.yijianguanzhu.transfer.server.connector;

import com.yijianguanzhu.transfer.server.connector.session.AbstractSession;
import com.yijianguanzhu.transfer.server.connector.session.Session;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author yijianguanzhu 2022年05月18日
 */
@Component
public class WebSocketConnector extends AbstractConnector {

	@Override
	public void close( String sessionId ) {
		Session session = sessionManager.getSession( sessionId );
		if ( Objects.nonNull( session ) ) {
			session.send( AppMessage.builder().cmd( CmdEnum.CLOSE ).code( CodeEnum.CLOSE ).build() );
			session.close();
			session.gc();
		}
	}

	@Override
	public void close( String sessionId, Message message ) {
		Session session = sessionManager.getSession( sessionId );
		if ( Objects.nonNull( session ) ) {
			session.send( message );
			close( sessionId );
		}
	}

	@Override
	public void keepalive( String sessionId ) {
		Session session = sessionManager.getSession( sessionId );
		if ( Objects.nonNull( session ) ) {
			AbstractSession abstractSession = ( AbstractSession ) session;
			abstractSession.setLastKeepaliveTime( LocalDateTime.now() );
			session.send( AppMessage.builder().cmd( CmdEnum.KEEPALIVE ).code( CodeEnum.SUCCESS ).build() );
		}
	}

	@Override
	public void keepaliveReq( String sessionId ) {
		Session session = sessionManager.getSession( sessionId );
		if ( Objects.nonNull( session ) ) {
			session.send( AppMessage.builder().cmd( CmdEnum.KEEPALIVE_REQ ).code( CodeEnum.SUCCESS ).build() );
		}
	}
}
