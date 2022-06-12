package com.yijianguanzhu.transfer.server.connector.session;

import java.util.List;

/**
 * @author yijianguanzhu 2022年05月18日
 */
public interface SessionManager {

	// 添加会话
	void addSession( Session session );

	// 获取会话
	Session getSession( String sessionId );

	// 获取所有会话
	List<Session> getAllSession();

	// 获取会话数
	int getSessionCount();

	// 会话保活
	void keepalive( Session session );

	// 会话保活
	void keepalive( String sessionId );

	// 删除会话
	void removeSession( Session session );

	// 删除会话
	void removeSession( String sessionId );
}
