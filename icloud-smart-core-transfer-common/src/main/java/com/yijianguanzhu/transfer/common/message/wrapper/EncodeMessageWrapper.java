package com.yijianguanzhu.transfer.common.message.wrapper;

import com.yijianguanzhu.transfer.common.utils.JsonUtil;
import com.yijianguanzhu.transfer.common.message.Message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author yijianguanzhu 2022年05月17日
 */
public final class EncodeMessageWrapper {

	// You can customize the message
	public static ByteBuffer encode( Message msg ) {
		byte[] bytes = JsonUtil.bean2byte( msg );
		int length = 4 + bytes.length;
		ByteBuffer buf = ByteBuffer.wrap( new byte[length] );
		return buffer( buf, length, bytes );
	}

	private static ByteBuffer buffer( ByteBuffer buf, int length, byte[] bytes ) {
		// 小端序
		buf.order( ByteOrder.LITTLE_ENDIAN );
		// 消息长度
		buf.putInt( length );
		// 数据部分
		buf.put( bytes );
		buf.flip();
		return buf;
	}
}
