package com.yijianguanzhu.transfer.server.handler;

import com.yijianguanzhu.transfer.common.utils.ContextUtil;
import com.yijianguanzhu.transfer.server.connector.AbstractConnector;
import com.yijianguanzhu.transfer.server.connector.session.AbstractSessionManager;
import com.yijianguanzhu.transfer.server.connector.session.Session;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.NetUtil;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * 监听连接处理器
 *
 * @author yijianguanzhu 2022年05月18日
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class EventListenerHandler extends ChannelInboundHandlerAdapter {

	@Autowired
	private AbstractConnector connector;

	@Autowired
	private AbstractSessionManager sessionManager;

	@Override
	public void channelRegistered( ChannelHandlerContext ctx ) throws Exception {
		log.debug( "The client({}) connected.",
				NetUtil.toSocketAddressString( ( InetSocketAddress ) ctx.channel().remoteAddress() ) );
		ctx.fireChannelRegistered();
	}

	@Override
	public void channelUnregistered( ChannelHandlerContext ctx ) throws Exception {
		log.debug( "The client({}) disconnected.",
				NetUtil.toSocketAddressString( ( InetSocketAddress ) ctx.channel().remoteAddress() ) );
		ctx.fireChannelUnregistered();
	}

	@Override
	public void channelActive( ChannelHandlerContext ctx ) throws Exception {
		log.debug( "The channel active from client({}).",
				NetUtil.toSocketAddressString( ( InetSocketAddress ) ctx.channel().remoteAddress() ) );
		ctx.fireChannelActive();
	}

	/**
	 * 运行过程中，连接主动断开/远程断开连接，会触发这个方法
	 */
	@Override
	public void channelInactive( ChannelHandlerContext ctx ) throws Exception {
		log.debug( "The channel inactive from client({}), the channel is about to close.",
				NetUtil.toSocketAddressString( ( InetSocketAddress ) ctx.channel().remoteAddress() ) );
		ctx.fireChannelInactive();
		String sessionId = ContextUtil.getChannelSessionHook( ctx );
		if ( !StringUtil.isNullOrEmpty( sessionId ) ) {
			Session session = sessionManager.getSession( sessionId );
			if ( Objects.nonNull( session ) && session.getConnection().getNative() == ctx )
				connector.close( sessionId );
		}
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception {
		log.error( "The client({}) unexpected error occurred.",
				NetUtil.toSocketAddressString( ( InetSocketAddress ) ctx.channel().remoteAddress() ), cause );
		ctx.fireExceptionCaught( cause );
	}

	@Override
	public void userEventTriggered( ChannelHandlerContext ctx, Object evt ) throws Exception {

		// 握手超时事件
		if ( evt instanceof WebSocketServerProtocolHandler.ServerHandshakeStateEvent ) {
			WebSocketServerProtocolHandler.ServerHandshakeStateEvent event = ( WebSocketServerProtocolHandler.ServerHandshakeStateEvent ) evt;
			if ( event == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_TIMEOUT ) {
				log.warn( "The client({}) handshake was timed out, the channel is about to close.",
						NetUtil.toSocketAddressString( ( InetSocketAddress ) ctx.channel().remoteAddress() ) );
				ctx.close();
			}
		}
		// 握手完成事件
		if ( evt instanceof WebSocketServerProtocolHandler.HandshakeComplete ) {
			WebSocketServerProtocolHandler.HandshakeComplete event = ( WebSocketServerProtocolHandler.HandshakeComplete ) evt;
			String sessionId = UUID.randomUUID().toString();
			connector.connect( ctx, sessionId );
			ContextUtil.setChannelSessionHook( ctx, sessionId );
			log.debug( "The client({}) handshake was completed successfully and the channel was upgraded to websockets.",
					NetUtil.toSocketAddressString( ( InetSocketAddress ) ctx.channel().remoteAddress() ) );
		}
		ctx.fireUserEventTriggered( evt );
	}
}
