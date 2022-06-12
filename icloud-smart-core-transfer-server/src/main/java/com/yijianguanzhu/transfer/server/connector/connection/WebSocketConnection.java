package com.yijianguanzhu.transfer.server.connector.connection;

import com.yijianguanzhu.transfer.server.connector.session.Session;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yijianguanzhu 2022年05月18日
 */
public class WebSocketConnection extends AbstractConnection {

	public WebSocketConnection( Session session, ChannelHandlerContext context ) {
		super( session, context );
	}
}
