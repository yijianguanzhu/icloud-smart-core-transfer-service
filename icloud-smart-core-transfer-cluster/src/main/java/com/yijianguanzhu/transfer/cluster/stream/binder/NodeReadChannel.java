package com.yijianguanzhu.transfer.cluster.stream.binder;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * 从消息中间件读取数据，可在这里自定义扩展
 *
 * @author yijianguanzhu 2023年01月12日
 */
public interface NodeReadChannel {

	String INPUT = "input";

	@Input(INPUT)
	SubscribableChannel input();
}
