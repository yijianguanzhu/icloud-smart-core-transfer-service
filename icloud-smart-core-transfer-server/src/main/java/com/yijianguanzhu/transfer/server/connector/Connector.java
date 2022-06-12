package com.yijianguanzhu.transfer.server.connector;

import com.yijianguanzhu.transfer.common.message.Message;

/**
 * @author yijianguanzhu 2022年05月18日
 */
public interface Connector {

	void send( String sessionId, Message message );
}
