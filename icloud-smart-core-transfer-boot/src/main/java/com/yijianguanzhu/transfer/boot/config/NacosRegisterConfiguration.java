package com.yijianguanzhu.transfer.boot.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.yijianguanzhu.transfer.common.props.TransferProperties;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * @author yijianguanzhu 2022年05月16日
 */
@Configuration
public class NacosRegisterConfiguration implements ApplicationListener<ApplicationStartedEvent> {

	@Autowired
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Autowired
	private NacosRegistration registration;

	@Autowired
	private TransferProperties transferProperties;

	@SneakyThrows
	@Override
	public void onApplicationEvent( ApplicationStartedEvent applicationStartedEvent ) {
		/**
		 * @see com.alibaba.cloud.nacos.registry.NacosServiceRegistry#register(Registration)
		 */
		NamingService namingService = nacosServiceManager.getNamingService( nacosDiscoveryProperties.getNacosProperties() );
		String name = registration.getServiceId();
		String groupName = nacosDiscoveryProperties.getGroup();
		Instance instance = getNacosInstanceFromRegistration( registration, transferProperties.getPort() );
		//将服务注册到注册中心
		namingService.registerInstance( name, groupName, instance );
	}

	private Instance getNacosInstanceFromRegistration( Registration registration, int port ) {
		Instance instance = new Instance();
		instance.setIp( registration.getHost() );
		instance.setPort( port );
		instance.setWeight( nacosDiscoveryProperties.getWeight() );
		instance.setClusterName( nacosDiscoveryProperties.getClusterName() );
		instance.setMetadata( registration.getMetadata() );
		return instance;
	}
}
