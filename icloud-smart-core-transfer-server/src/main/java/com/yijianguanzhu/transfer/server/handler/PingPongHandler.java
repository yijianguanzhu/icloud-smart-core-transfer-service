package com.yijianguanzhu.transfer.server.handler;

import com.yijianguanzhu.transfer.common.utils.ContextUtil;
import com.yijianguanzhu.transfer.server.connector.AbstractConnector;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yijianguanzhu 2022年05月18日
 */
@Component
@ChannelHandler.Sharable
public class PingPongHandler extends ChannelDuplexHandler {

	@Autowired
	private AbstractConnector connector;

	@Override
	public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {
		if ( msg instanceof Message ) {
			AppMessage reqMsg = ( AppMessage ) msg;
			if ( reqMsg.getCmd() == CmdEnum.KEEPALIVE ) {
				connector.keepalive( ContextUtil.getChannelSessionHook( ctx ) );
				return;
			}
		}
		ctx.fireChannelRead( msg );
	}
}
