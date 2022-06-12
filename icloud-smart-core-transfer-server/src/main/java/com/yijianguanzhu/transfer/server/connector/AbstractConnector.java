package com.yijianguanzhu.transfer.server.connector;

import com.yijianguanzhu.transfer.server.connector.listener.OnlineListener;
import com.yijianguanzhu.transfer.server.connector.session.AbstractSessionManager;
import com.yijianguanzhu.transfer.server.connector.session.Session;
import com.yijianguanzhu.transfer.common.message.Message;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Objects;

/**
 * @author yijianguanzhu 2022年05月18日
 */
public abstract class AbstractConnector implements Connector {

	@Autowired
	protected AbstractSessionManager sessionManager;

	@Autowired
	@Lazy
	protected OnlineListener onlineListener;

	@Override
	public void send( String sessionId, Message message ) {
		Session session = sessionManager.getSession( sessionId );
		if ( Objects.nonNull( session ) ) {
			session.send( message );
		}
	}

	public void connect( ChannelHandlerContext context, String sessionId ) {
		sessionManager.connect( sessionId, context );
		keepaliveReq( sessionId );
		onlineListener.signal();
	}

	public abstract void close( String sessionId );

	public abstract void close( String sessionId, Message message );

	public abstract void keepalive( String sessionId );

	public abstract void keepaliveReq( String sessionId );
}
