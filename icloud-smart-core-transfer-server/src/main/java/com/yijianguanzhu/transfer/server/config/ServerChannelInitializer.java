package com.yijianguanzhu.transfer.server.config;

import com.yijianguanzhu.transfer.common.props.TransferProperties;
import com.yijianguanzhu.transfer.common.provider.HandlerProvider;
import com.yijianguanzhu.transfer.server.codec.ByteToMessageDecoder;
import com.yijianguanzhu.transfer.server.codec.MessageToMessageCodec;
import com.yijianguanzhu.transfer.server.codec.MessageToMessageDecoder;
import com.yijianguanzhu.transfer.server.handler.EventListenerHandler;
import com.yijianguanzhu.transfer.server.handler.PingPongHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yijianguanzhu 2022年05月18日
 */
@Component
@Slf4j
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Autowired
	private MessageToMessageCodec messageToMessageCodec;

	@Autowired
	private EventListenerHandler eventListenerHandler;

	@Autowired
	private MessageToMessageDecoder messageToMessageDecoder;

	@Autowired
	private PingPongHandler pingPongHandler;

	@Autowired
	private TransferProperties transferProperties;

	// 加载自定义handler组件
	List<HandlerProvider> handlerProviderList = new LinkedList<>();

	@PostConstruct
	private void loadingHandlerProvider() {
		ServiceLoader.load( HandlerProvider.class )
				.forEach( handlerProvider -> {
					Class<? extends HandlerProvider> providerClass = handlerProvider.getClass();
					boolean isSharable = providerClass.getAnnotation( Sharable.class ) != null;
					if ( isSharable && handlerProvider.getInstance() != handlerProvider ) {
						log.error( "This '{}' no needed to override the .getInstance() method as it's shared.", providerClass );
						return;
					}
					if ( !isSharable && handlerProvider.getInstance() == handlerProvider ) {
						log.error( "This '{}' need to override the .getInstance() method as it's not shared.", providerClass );
						return;
					}
					handlerProviderList.add( handlerProvider );
				} );
		this.handlerProviderList = handlerProviderList.stream().sorted( Comparator.comparing( HandlerProvider::getOrder ) )
				.collect( Collectors.toCollection( LinkedList::new ) );
	}

	@Override
	protected void initChannel( SocketChannel ch ) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast( new HttpServerCodec() );
		pipeline.addLast( new HttpObjectAggregator( 65536 ) );
		pipeline.addLast( new ChunkedWriteHandler() );
		pipeline.addLast( new WebSocketServerProtocolHandler( WebSocketServerProtocolConfig.newBuilder()
				.websocketPath( "/transfer" )
				.handshakeTimeoutMillis( transferProperties.getCore().getHandshakeTimeout() )
				.dropPongFrames( true )
				.maxFramePayloadLength( transferProperties.getCore().getMaxFramePayloadLength() )
				.build() ) );
		pipeline.addLast( messageToMessageCodec );
		pipeline.addLast( messageToMessageDecoder );
		pipeline.addLast( new ByteToMessageDecoder() );
		pipeline.addLast( eventListenerHandler );
		pipeline.addLast( pingPongHandler );
		handlerProviderList.forEach( handlerProvider -> pipeline.addLast( handlerProvider.getInstance() ) );
	}
}
