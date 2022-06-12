package com.yijianguanzhu.transfer.common.message.cluster;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author yijianguanzhu 2022年05月19日
 */
@Component
@Slf4j
public class ClusterProxy {

	private static ProducerProxy producerProxy;

	@Autowired(required = false)
	private void setProducerProxy( ProducerProxy sp ) {
		if ( Objects.nonNull( sp ) ) {
			log.info( sp.getClass().toString() );
			producerProxy = sp;
		}
	}

	public static boolean send( Object clusterMessage ) {
		return Objects.nonNull( producerProxy ) && producerProxy.produce( clusterMessage );
	}
}
