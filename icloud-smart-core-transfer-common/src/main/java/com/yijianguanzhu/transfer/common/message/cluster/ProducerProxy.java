package com.yijianguanzhu.transfer.common.message.cluster;

/**
 * 集群消息发送代理
 *
 * @author yijianguanzhu 2022年06月02日
 */
public interface ProducerProxy {

	boolean produce( Object clusterMessage );
}
