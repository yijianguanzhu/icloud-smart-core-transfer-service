package com.yijianguanzhu.transfer.server.test.client;

import com.yijianguanzhu.transfer.common.utils.JsonUtil;
import com.yijianguanzhu.transfer.server.codec.ByteToMessageDecoder;
import com.yijianguanzhu.transfer.server.codec.MessageToMessageCodec;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import com.yijianguanzhu.transfer.server.message.body.AuthBody;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * netty websocket 客户端测试
 *
 * @author yijianguanzhu 2022年06月10日
 */
@Slf4j
public class NettyWebSocketClient {

	// 网关 host
	public static String host = "127.0.0.1";
	// 网关 port
	public static int port = 18100;
	public static String gatewayUri = String.format( "ws://%s:%s/transfer-service/transfer", host, port );
	public static NioEventLoopGroup GROUP = new NioEventLoopGroup();
	public static Bootstrap BOOTSTRAP = new Bootstrap().group( GROUP ).channel( NioSocketChannel.class )
			.option( ChannelOption.TCP_NODELAY, false )
			.option( ChannelOption.SO_KEEPALIVE, true )
			.option( ChannelOption.SO_REUSEADDR, false )
			.handler( new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel( SocketChannel ch ) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast( new HttpClientCodec() );
					pipeline.addLast( new HttpObjectAggregator( 65536 ) );
					pipeline.addLast( new WebSocketClientProtocolHandler( WebSocketClientProtocolConfig.newBuilder()
							.webSocketUri( URI.create( gatewayUri ) )
							.handshakeTimeoutMillis( 5000L )
							.build() ) );
					pipeline.addLast( new MessageToMessageCodec() );
					pipeline.addLast( new ByteToMessageDecoder() );
					pipeline.addLast( new HeartbeatListenerHandler() );
				}
			} );

	public static void main( String[] args ) throws InterruptedException {
		ChannelFuture connect;
		try {
			connect = BOOTSTRAP.connect( host, port ).sync();
		}
		catch ( Exception e ) {
			log.error( "连接服务端失败", e );
			return;
		}
		log.info( "连接服务端成功" );
		Channel channel = connect.channel();
		// 5秒后，账号登陆
		TimeUnit.SECONDS.sleep( 5 );
		String body = JsonUtil.bean2json( AuthBody.builder().token( "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsIm5iZiI6MTY1NDg2NDIzMSwidXNlcl9pZCI6MTAwMCwidXNlcl9uYW1lIjoiYWRtaW4iLCJjcmVhdGVkIjp7fSwiZXhwIjoxNjU1MTIzNDMxfQ.T0kV3p5UX-mgoFCZMWH04--kQ7ciF3qAQNX2Rk9yJyo" ).build() );
		channel.writeAndFlush( AppMessage.builder().cmd( CmdEnum.LOGIN ).body( body ).build() );
	}
}
