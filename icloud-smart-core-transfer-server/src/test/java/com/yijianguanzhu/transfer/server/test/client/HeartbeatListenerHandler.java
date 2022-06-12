package com.yijianguanzhu.transfer.server.test.client;

import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 客户端心跳
 *
 * @author yijianguanzhu 2022年06月10日
 */
public class HeartbeatListenerHandler extends ChannelDuplexHandler {

	@Override
	public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {
		if ( msg instanceof Message ) {
			AppMessage reqMsg = ( AppMessage ) msg;
			if ( reqMsg.getCmd() == CmdEnum.KEEPALIVE_REQ ) {
				ctx.writeAndFlush( AppMessage.builder().cmd( CmdEnum.KEEPALIVE ).build() );
				return;
			}
		}
		ctx.fireChannelRead( msg );
	}
}
