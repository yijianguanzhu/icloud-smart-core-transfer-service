package com.yijianguanzhu.transfer.common.message.wrapper;

import com.yijianguanzhu.transfer.common.utils.JsonUtil;
import com.yijianguanzhu.transfer.common.message.Message;

/**
 * @author yijianguanzhu 2022年05月17日
 */
public final class DecodeMessageWrapper {

	public static Message decode( byte[] array, Class<? extends Message> clazz ) {
		return JsonUtil.byte2bean( array, clazz );
	}
}
