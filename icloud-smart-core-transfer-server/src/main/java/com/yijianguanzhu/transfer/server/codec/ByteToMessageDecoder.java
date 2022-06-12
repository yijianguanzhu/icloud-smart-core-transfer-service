package com.yijianguanzhu.transfer.server.codec;

import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import com.yijianguanzhu.transfer.common.message.wrapper.DecodeMessageWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

/**
 * 消息读写半包处理
 *
 * @author yijianguanzhu 2022年05月18日
 */
@Slf4j
public class ByteToMessageDecoder extends io.netty.handler.codec.ByteToMessageDecoder {

	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out )
			throws Exception {
		final ByteBuf decoded = decode( ctx, in );
		if ( Objects.nonNull( decoded ) ) {
			Message msg;
			try {
				msg = DecodeMessageWrapper.decode( decoded.array(), AppMessage.class );
			}
			catch ( Exception e ) {
				log.error( "client({}) sent invalid binary data source",
						NetUtil.toSocketAddressString( ( InetSocketAddress ) ctx.channel().remoteAddress() ), e );
				// 关闭连接
				ctx.writeAndFlush( AppMessage.builder()
						.cmd( CmdEnum.ERROR )
						.code( CodeEnum.ERR_BINARY_FRAME )
						.build() );
				ctx.writeAndFlush( AppMessage.builder()
						.cmd( CmdEnum.CLOSE )
						.code( CodeEnum.CLOSE )
						.build() );
				ctx.close();
				return;
			}
			out.add( msg );
		}
	}

	// 实际解码工作
	protected ByteBuf decode( final ChannelHandlerContext ctx, final ByteBuf in ) throws Exception {
		int messageLength = getMessageLength( in );
		if ( messageLength == -1 ) {
			// 可查看是否拆包
			if ( log.isDebugEnabled() ) {
				log.debug( "ws 拆包， 消息总长度：{}，本次读取消息长度：{}", in.capacity(), messageLength );
			}
			return null;
		}

		// 可查看是否粘包
		if ( log.isDebugEnabled() ) {
			if ( messageLength - 4 != in.readableBytes() ) {
				log.debug( "ws 粘包， 消息总长度：{}，本次读取消息长度：{}", in.capacity(), messageLength );
			}
			else {
				if ( messageLength == in.capacity() ) {
					log.debug( "ws 未粘包， 消息总长度：{}，本次读取消息长度：{}", in.capacity(), messageLength );
				}
				else {
					log.debug( "ws 粘包处理完成， 消息总长度：{}，本次读取消息长度：{}", in.capacity(), messageLength );
				}
			}
		}

		// extract frame
		int readerIndex = in.readerIndex();
		ByteBuf frame = extractFrame( ctx, in, readerIndex, messageLength - 4 );
		in.readerIndex( readerIndex + messageLength - 4 );
		return frame;
	}

	/**
	 * 获取单条消息总长度
	 */
	protected int getMessageLength( ByteBuf in ) {
		// 剩余可读字节数
		int remainingReadableBytes = in.readableBytes();
		if ( remainingReadableBytes < 4 ) {
			return -1;
		}

		in.markReaderIndex();
		int messageLength = in.readIntLE();
		if ( remainingReadableBytes < messageLength ) {
			in.resetReaderIndex();
			return -1;
		}
		return messageLength;
	}

	/**
	 * 获取装载真实数据的ByteBuf
	 */
	protected ByteBuf extractFrame( ChannelHandlerContext ctx, ByteBuf buffer, int index, int length ) {
		// copy direct bytebuf convert heap bytebuf.
		ByteBuf buf = buffer.retainedSlice( index, length );
		try {
			return Unpooled.copiedBuffer( buf );
		}
		finally {
			if ( buf != null ) {
				// release direct bytebuf
				buf.release();
			}
		}
	}
}
