package com.yijianguanzhu.transfer.server.connector.session;

import java.time.LocalDateTime;

/**
 * @author yijianguanzhu 2022年05月18日
 */
public interface SessionValid {

	// 设置会话创建时间
	void setCreationTime( LocalDateTime creationTime );

	// 获取连接时间
	LocalDateTime getCreationTime();

	// 设置最新的心跳时间
	void setLastKeepaliveTime( LocalDateTime lastKeepaliveTime );

	// 获取上次心跳时间
	LocalDateTime getLastKeepaliveTime();

	// 会话是否过期（会话心跳过期）
	boolean isExpire();

	// 设置保活间隔
	void setKeepaliveInterval( long seconds );

	// 获取保活间隔
	long getKeepaliveInterval();

	// 设置认证状态
	void setAuthenticated( boolean isAuthenticated );

	// 设置认证过期时间
	void setAuthExpireTime( LocalDateTime authExpireTime );

	// 认证过期时间
	LocalDateTime getAuthExpireTime();

	// 是否认证
	boolean isAuthenticated();
}
