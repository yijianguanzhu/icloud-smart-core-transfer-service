package com.yijianguanzhu.transfer.cluster.stream.binder;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * 向消息中间件发送数据，可在这里自定义扩展
 *
 * @author yijianguanzhu 2023年01月12日
 */
public interface NodeSendChannel {

	String OUTPUT = "output";

	@Output(OUTPUT)
	MessageChannel output();
}
