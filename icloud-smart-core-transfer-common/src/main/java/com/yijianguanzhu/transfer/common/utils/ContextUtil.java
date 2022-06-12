package com.yijianguanzhu.transfer.common.utils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * @author yijianguanzhu 2022年05月17日
 */
public class ContextUtil {

	public static final AttributeKey<String> WEBSOCKET_CHANNEL_SESSION_HOOK = AttributeKey
			.valueOf( "WEBSOCKET_CHANNEL_SESSION_HOOK" );

	public static String getChannelHook( ChannelHandlerContext ctx, AttributeKey<String> attr ) {
		return ctx.channel().attr( attr ).get();
	}

	public static String getChannelSessionHook( ChannelHandlerContext ctx ) {
		return getChannelHook( ctx, WEBSOCKET_CHANNEL_SESSION_HOOK );
	}

	public static void setChannelHook( ChannelHandlerContext ctx, AttributeKey<String> attr, String val ) {
		ctx.channel().attr( attr ).set( val );
	}

	public static void setChannelSessionHook( ChannelHandlerContext ctx, String sessionId ) {
		setChannelHook( ctx, WEBSOCKET_CHANNEL_SESSION_HOOK, sessionId );
	}
}
