package com.yijianguanzhu.transfer.server.connector.session;

import com.yijianguanzhu.transfer.common.props.TransferProperties;
import com.yijianguanzhu.transfer.server.connector.connection.WebSocketConnection;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author yijianguanzhu 2022年05月18日
 */
@Component
public class WebSocketSessionManager extends AbstractSessionManager {

	@Autowired
	private TransferProperties transferProperties;

	@Override
	public Session connect( String sessionId, ChannelHandlerContext ctx ) {
		WebSocketSession session = new WebSocketSession();
		session.setCreationTime( LocalDateTime.now() );
		session.setLastKeepaliveTime( LocalDateTime.now() );
		session.setKeepaliveInterval( transferProperties.getSession().getKeepaliveInterval() );
		session.setAuthenticated( false );
		session.setSessionId( sessionId );
		session.setSessionManager( this );
		session.setConnection( new WebSocketConnection( session, ctx ) );
		this.addSession( session );
		return session;
	}
}
