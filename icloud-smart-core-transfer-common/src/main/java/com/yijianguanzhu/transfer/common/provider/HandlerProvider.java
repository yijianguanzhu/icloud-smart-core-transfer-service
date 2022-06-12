package com.yijianguanzhu.transfer.common.provider;

import io.netty.channel.ChannelHandler;
import org.springframework.core.Ordered;

/**
 * netty handler spi扩展器，用于组件发现
 *
 * @author yijianguanzhu 2022年05月18日
 */
public interface HandlerProvider extends Ordered, Comparable<HandlerProvider>, ChannelHandler {

	default HandlerProvider getInstance() {
		return this;
	}

	/**
	 * 获取排列顺序
	 */
	@Override
	default int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	/**
	 * 对比排序
	 */
	default int compareTo( HandlerProvider o ) {
		return Integer.compare( this.getOrder(), o.getOrder() );
	}
}
