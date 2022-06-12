package com.yijianguanzhu.transfer.server.connector.session;

import com.yijianguanzhu.transfer.server.connector.connection.Connection;
import com.yijianguanzhu.transfer.common.message.Message;

/**
 * @author yijianguanzhu 2022年05月18日
 */
public interface Session extends SessionValid {

	// 添加连接
	void setConnection( Connection connection );

	// 获取连接
	Connection getConnection();

	// 发送消息
	void send( Message message );

	// 关闭会话
	void close();

	// 手动gc
	void gc();

	void setSessionId( String sessionId );

	String getSessionId();

	void setSessionManager( SessionManager sessionManager );

	SessionManager getSessionManager();
}
