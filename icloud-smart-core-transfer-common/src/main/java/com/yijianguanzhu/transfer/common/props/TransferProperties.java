package com.yijianguanzhu.transfer.common.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

/**
 * @author yijianguanzhu 2022年05月17日
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "transfer-service")
public class TransferProperties {

	// port
	private int port = 6014;

	private SessionProperties session = new SessionProperties();

	private TimeWheelProperties timeWheel = new TimeWheelProperties();

	private CoreProperties core = new CoreProperties();

	private ClusterProperties cluster = new ClusterProperties();

	@Getter
	@Setter
	public static class SessionProperties {

		// 会话保活间隔 单位/秒
		private long keepaliveInterval = 45;
	}

	@Getter
	@Setter
	public static class TimeWheelProperties {

		// 心跳检查间隔 单位/秒
		private long keepaliveInterval = 45;

		// 最大心跳离线时间 单位/秒
		private long maxKeepaliveInterval = 120;
	}

	@Getter
	@Setter
	public static class CoreProperties {

		// netty bossThread线程数
		private int bossThread = Runtime.getRuntime().availableProcessors();

		// netty workerThread线程数
		private int workerThread = bossThread * 2;

		// websocket 握手超时时间（单位/秒）
		private long handshakeTimeout = 5000L;

		// websocket 最大负载
		private int maxFramePayloadLength = 60 * 1024 * 1024;
	}

	@Getter
	@Setter
	@Deprecated
	public static class RabbitProperties {
		// 是否启用rabbit
		private boolean enable = false;
	}

	@Getter
	@Setter
	public static class ClusterProperties {

		private RabbitProperties rabbit = new RabbitProperties();

		// 集群节点id，区分各个节点，不可重复
		private String instanceId = UUID.randomUUID().toString();
	}
}
