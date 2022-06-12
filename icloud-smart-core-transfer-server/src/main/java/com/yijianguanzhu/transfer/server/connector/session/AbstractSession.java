package com.yijianguanzhu.transfer.server.connector.session;

import com.yijianguanzhu.transfer.server.connector.connection.Connection;
import com.yijianguanzhu.transfer.common.message.Message;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author yijianguanzhu 2022年05月18日
 */
public abstract class AbstractSession implements Session {

	protected SessionManager sessionManager;

	private String sessionId;

	private Connection connection;

	private volatile boolean isAuthenticated = false;

	private volatile LocalDateTime authExpireTime = LocalDateTime.now();

	private volatile LocalDateTime lastKeepaliveTime = LocalDateTime.now();

	private LocalDateTime creationTime;

	protected long keepaliveInterval;

	@Override
	public void setConnection( Connection connection ) {
		this.connection = connection;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public void send( Message message ) {
		getConnection().send( message );
	}

	@Override
	public boolean isAuthenticated() {
		return isAuthenticated && authExpireTime.isAfter( LocalDateTime.now() );
	}

	@Override
	public void setAuthenticated( boolean isAuthenticated ) {
		this.isAuthenticated = isAuthenticated;
	}

	@Override
	public LocalDateTime getAuthExpireTime() {
		return authExpireTime;
	}

	@Override
	public LocalDateTime getLastKeepaliveTime() {
		return lastKeepaliveTime;
	}

	@Override
	public void setLastKeepaliveTime( LocalDateTime currKeepaliveTime ) {
		this.lastKeepaliveTime = currKeepaliveTime;
	}

	@Override
	public void setAuthExpireTime( LocalDateTime authExpireTime ) {
		this.authExpireTime = authExpireTime;
	}

	@Override
	public void close() {
		connection.close();
	}

	@Override
	public void gc() {
		sessionManager.removeSession( this );
	}

	@Override
	public boolean isExpire() {
		return Duration.between( lastKeepaliveTime, LocalDateTime.now() ).getSeconds() > keepaliveInterval;
	}

	@Override
	public void setSessionId( String sessionId ) {
		this.sessionId = sessionId;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public void setSessionManager( SessionManager sessionManager ) {
		this.sessionManager = sessionManager;
	}

	@Override
	public SessionManager getSessionManager() {
		return sessionManager;
	}

	@Override
	public void setCreationTime( LocalDateTime creationTime ) {
		this.creationTime = creationTime;
	}

	@Override
	public LocalDateTime getCreationTime() {
		return creationTime;
	}

	@Override
	public void setKeepaliveInterval( long seconds ) {
		keepaliveInterval = seconds;
	}

	@Override
	public long getKeepaliveInterval() {
		return keepaliveInterval;
	}
}
