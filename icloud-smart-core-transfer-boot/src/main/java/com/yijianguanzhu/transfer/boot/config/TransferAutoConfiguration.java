package com.yijianguanzhu.transfer.boot.config;

import com.yijianguanzhu.transfer.common.props.TransferProperties;
import com.yijianguanzhu.transfer.server.config.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author yijianguanzhu 2022年05月17日
 */
@Configuration
@EnableConfigurationProperties(value = TransferProperties.class)
@Slf4j
public class TransferAutoConfiguration {

	@Autowired
	private TransferProperties transferProperties;

	@Autowired
	private ServerChannelInitializer serverChannelInitializer;

	// 启动服务端
	@PostConstruct
	public void startupServer() {
		NioEventLoopGroup bossGroup = new NioEventLoopGroup( transferProperties.getCore().getBossThread() );
		NioEventLoopGroup workGroup = new NioEventLoopGroup( transferProperties.getCore().getWorkerThread() );
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group( bossGroup, workGroup )
				.channel( NioServerSocketChannel.class )
				.childHandler( serverChannelInitializer )
				.childOption( ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT )
				.childOption( ChannelOption.TCP_NODELAY, false )
				.childOption( ChannelOption.SO_KEEPALIVE, true )
				.childOption( ChannelOption.SO_REUSEADDR, false )
				.bind( transferProperties.getPort() )
				.addListener( f -> {
					if ( f.isSuccess() ) {
						log.info( "Server have success bind to: {}", transferProperties.getPort() );
					}
					else {
						log.error( "Server failed bind to: {} ", transferProperties.getPort(), f.cause() );
						log.info( "shutdown server ..." );
						// 释放线程池资源
						bossGroup.shutdownGracefully();
						workGroup.shutdownGracefully();
						log.info( "shutdown server end." );
					}
				} );
	}
}
