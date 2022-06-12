package com.yijianguanzhu.transfer.server.connector.connection;

import com.yijianguanzhu.transfer.server.connector.session.Session;
import com.yijianguanzhu.transfer.common.message.Message;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author yijianguanzhu 2022年05月18日
 */
@Slf4j
public abstract class AbstractConnection implements Connection {

	protected volatile Session session;

	protected volatile ChannelHandlerContext context;

	public AbstractConnection( Session session, ChannelHandlerContext context ) {
		this.session = session;
		this.context = context;
	}

	@Override
	public void send( Message message ) {
		if ( Objects.nonNull( context ) ) {
			try {
				context.writeAndFlush( message );
			}
			catch ( Exception e ) {
				log.error( "connection send message occur Throwable.", e );
			}
		}
	}

	@Override
	public void close() {
		context.close();
	}

	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public Object getNative() {
		return context;
	}
}
