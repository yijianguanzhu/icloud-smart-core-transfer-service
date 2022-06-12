package com.yijianguanzhu.transfer.server.codec;

import com.yijianguanzhu.transfer.common.utils.ContextUtil;
import com.yijianguanzhu.transfer.server.connector.AbstractConnector;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author yijianguanzhu 2022年05月18日
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class MessageToMessageDecoder extends io.netty.handler.codec.MessageToMessageDecoder<TextWebSocketFrame> {

	@Autowired
	private AbstractConnector connector;

	@Override
	protected void decode( ChannelHandlerContext ctx, TextWebSocketFrame msg, List<Object> out ) throws Exception {
		log.info( "client({}) sent invalid text frame data source, close the channel.",
				NetUtil.toSocketAddressString( ( InetSocketAddress ) ctx.channel().remoteAddress() ) );
		connector.close( ContextUtil.getChannelSessionHook( ctx ),
				AppMessage.builder().cmd( CmdEnum.ERROR ).code( CodeEnum.ERR_TEXT_FRAME ).build() );
	}
}
