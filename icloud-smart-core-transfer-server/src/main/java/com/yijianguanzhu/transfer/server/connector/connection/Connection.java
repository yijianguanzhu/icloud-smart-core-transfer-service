package com.yijianguanzhu.transfer.server.connector.connection;

import com.yijianguanzhu.transfer.server.connector.session.Session;
import com.yijianguanzhu.transfer.common.message.Message;

/**
 * @author yijianguanzhu 2022年05月18日
 */
public interface Connection {

	// 发送消息
	void send( Message message );

	// 关闭连接
	void close();

	// 获取会话
	Session getSession();

	// 获取本地连接
	Object getNative();
}
