package com.yijianguanzhu.transfer.server.codec;

import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.common.message.wrapper.EncodeMessageWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 消息编解码器
 *
 * @author yijianguanzhu 2022年05月18日
 */
@ChannelHandler.Sharable
@Component
public class MessageToMessageCodec extends io.netty.handler.codec.MessageToMessageCodec<BinaryWebSocketFrame, Message> {

	/**
	 * 将消息转成 {@link BinaryWebSocketFrame}
	 */
	@Override
	protected void encode( ChannelHandlerContext ctx, Message msg, List<Object> out ) throws Exception {
		ByteBuffer encode = EncodeMessageWrapper.encode( msg );
		ByteBuf buf = ctx.alloc().buffer().writeBytes( encode );
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame( buf );
		out.add( frame );
	}

	/**
	 * 将 {@link BinaryWebSocketFrame}转成 {@link ByteBuf}
	 */
	@Override
	protected void decode( ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out )
			throws Exception {
		ByteBuf content = msg.content();
		// 持有Bytebuf，因为上层会在此方法结束后，释放msg。
		ReferenceCountUtil.retain( content );
		out.add( content );
	}
}
