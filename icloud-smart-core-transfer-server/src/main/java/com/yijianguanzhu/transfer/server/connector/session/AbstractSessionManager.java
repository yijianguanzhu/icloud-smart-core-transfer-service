package com.yijianguanzhu.transfer.server.connector.session;

import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yijianguanzhu 2022年05月18日
 */
public abstract class AbstractSessionManager implements SessionManager {

	protected Map<String, Session> sessions = new ConcurrentHashMap<>();

	@Override
	public void addSession( Session session ) {
		sessions.put( session.getSessionId(), session );
	}

	public abstract Session connect( String sessionId, ChannelHandlerContext ctx );

	@Override
	public Session getSession( String sessionId ) {
		return sessions.get( sessionId );
	}

	@Override
	public List<Session> getAllSession() {
		return new LinkedList<>( sessions.values() );
	}

	@Override
	public int getSessionCount() {
		return sessions.size();
	}

	@Override
	public void keepalive( Session session ) {
		session.send( AppMessage.builder()
				.cmd( CmdEnum.KEEPALIVE )
				.code( CodeEnum.SUCCESS )
				.build() );
	}

	@Override
	public void keepalive( String sessionId ) {
		keepalive( getSession( sessionId ) );
	}

	@Override
	public void removeSession( Session session ) {
		sessions.remove( session.getSessionId() );
	}

	@Override
	public void removeSession( String sessionId ) {
		sessions.remove( sessionId );
	}
}
