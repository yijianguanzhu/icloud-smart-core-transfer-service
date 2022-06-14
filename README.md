### icloud smart core transfer service项目介绍
* 没有前端。
* 后端采用Netty开发websocket，支持集群，支持将服务注册到nacos注册中心。
* 整合了springboot，使用它的自动装配等功能，不包含它的webmvc模块，没有servlet，只有纯纯ws协议，不支持restful 此类http协议。
* 后端采用feign与nacos中其他服务通信，支持熔断降级，支持负载均衡。
* 其他服务可到 [icloud-simple-service](https://github.com/yijianguanzhu/icloud-simple-service) 查看，包含网关、认证中心、用户中心服务，为本项目而生，当然也可以用作其他用途。
* 项目使用模块分包，推荐使用[IntelliJ IDEA](https://www.jetbrains.com/idea)打开。
* 服务启动完成后，进入 `icloud-smart-core-transfer-server`模块下的test包，找到 `NettyWebSocketClient` 类对websocket server进行测试。

### 工程结构
``` 
icloud-smart-core-transfer-service
├── icloud-smart-core-auth -- 用户登录模块
├── icloud-smart-core-boot -- 启动类模块
├── icloud-smart-core-client -- feign client模块
├── icloud-smart-core-cluster -- 集群模块
├── icloud-smart-core-common -- 基础工具类模块
├── icloud-smart-core-server -- websocket server模块
├── icloud-smart-core-service-api -- 业务模块api
├    ├── icloud-smart-core-cluster-api -- 集群模块api
└──  └── icloud-smart-core-servler-api -- websocket server模块api 
```

### 开源协议
Apache Licence 2.0 （[英文原文](http://www.apache.org/licenses/LICENSE-2.0.html)）
Apache Licence是著名的非盈利开源组织Apache采用的协议。该协议和BSD类似，同样鼓励代码共享和尊重原作者的著作权，同样允许代码修改，再发布（作为开源或商业软件）。
需要满足的条件如下：
* 需要给代码的用户一份Apache Licence
* 如果你修改了代码，需要在被修改的文件中说明。
* 在延伸的代码中（修改和有源代码衍生的代码中）需要带有原来代码中的协议，商标，专利声明和其他原来作者规定需要包含的说明。
* 如果再发布的产品中包含一个Notice文件，则在Notice文件中需要带有Apache Licence。你可以在Notice中增加自己的许可，但不可以表现为对Apache Licence构成更改。
Apache Licence也是对商业应用友好的许可。使用者也可以在需要的时候修改代码来满足需要并作为开源或商业产品发布/销售。

### 用户权益
* 允许免费用于学习、毕设、公司项目、私活等。
* 对未经过授权和不遵循 Apache 2.0 协议二次开源或者商业化作者将追究到底。
* 若禁止条款被发现有权追讨 **5000** 的授权费。

### Netty Reactor 主从多线程网络模型
![Netty Reactor主从多线程网络模型](https://user-images.githubusercontent.com/68835311/173493168-c87515f9-9f05-43c9-95b4-a0f1d1b6c2de.png)

### 代码介绍
* 消息编解码器 
  * 服务收发消息采用二进制帧，一段完整的二进制数据包由【头部+载体】两部分组成：
    * 头部：4 字节小端整数，表示整条消息（包括自身）长度（字节数）。
    * 载体：被序列化后的负载数据。
```
package com.yijianguanzhu.transfer.server.codec;

import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.common.message.wrapper.EncodeMessageWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 消息编解码器
 *
 * @author yijianguanzhu 2022年05月18日
 */
@ChannelHandler.Sharable
@Component
public class MessageToMessageCodec extends io.netty.handler.codec.MessageToMessageCodec<BinaryWebSocketFrame, Message> {

	/**
	 * 将消息转成 {@link BinaryWebSocketFrame}
	 */
	@Override
	protected void encode( ChannelHandlerContext ctx, Message msg, List<Object> out ) throws Exception {
		ByteBuffer encode = EncodeMessageWrapper.encode( msg );
		ByteBuf buf = ctx.alloc().buffer().writeBytes( encode );
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame( buf );
		out.add( frame );
	}

	/**
	 * 将 {@link BinaryWebSocketFrame}转成 {@link ByteBuf}
	 */
	@Override
	protected void decode( ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out )
			throws Exception {
		ByteBuf content = msg.content();
		// 持有Bytebuf，因为上层会在此方法结束后，释放msg。
		ReferenceCountUtil.retain( content );
		out.add( content );
	}
}

```

* 消息读写半包处理
    * 总体思路：
        * 根据消息编解码机制提取头部数据，由头部数据长度判断该条消息是否粘包拆包。
        * 再由头部数据长度，提取有效负载数据。
        * 反序列化负载数据，报错直接关闭连接。
```
package com.yijianguanzhu.transfer.server.codec;

import com.yijianguanzhu.transfer.common.utils.ContextUtil;
import com.yijianguanzhu.transfer.common.utils.SpringUtil;
import com.yijianguanzhu.transfer.server.connector.AbstractConnector;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import com.yijianguanzhu.transfer.common.message.wrapper.DecodeMessageWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
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
				AbstractConnector connector = SpringUtil.getBean( AbstractConnector.class );
				connector.close( ContextUtil.getChannelSessionHook( ctx ), AppMessage.builder()
						.cmd( CmdEnum.ERROR ).code( CodeEnum.ERR_BINARY_FRAME ).build() );
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

```

* 心跳检测
    * 总体思路：
        * 由单独线程管理所有 session 心跳
        * 在允许客户端离线的最大间隔内，提醒客户端回复心跳
        * 超过最大离线间隔的客户端将踢下线
```
package com.yijianguanzhu.transfer.server.connector.listener;

import com.yijianguanzhu.transfer.common.props.TransferProperties;
import com.yijianguanzhu.transfer.server.connector.AbstractConnector;
import com.yijianguanzhu.transfer.server.connector.session.AbstractSession;
import com.yijianguanzhu.transfer.server.connector.session.AbstractSessionManager;
import com.yijianguanzhu.transfer.server.connector.session.Session;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yijianguanzhu 2022年05月18日
 */
@Slf4j
@Component
public class OnlineListener implements Runnable {

	@Autowired
	protected AbstractSessionManager sessionManager;

	@Autowired
	private AbstractConnector connector;

	@Autowired
	private TransferProperties transferProperties;

	private ReentrantLock lock = new ReentrantLock();

	private Condition notEmpty = lock.newCondition();

	// 检测心跳间隔
	private long period;

	// 允许心跳的最长离线间隔
	private long lose;

	@PostConstruct
	public void init() {
		this.period = transferProperties.getTimeWheel().getKeepaliveInterval();
		this.lose = transferProperties.getTimeWheel().getMaxKeepaliveInterval();
		Thread onlineListener = new Thread( this, "OnlineListener" );
		onlineListener.setDaemon( true );
		onlineListener.start();
	}

	@Override
	public void run() {
		try {
			while ( true ) {
				if ( sessionManager.getSessionCount() == 0 ) {
					this.await();
				}
				log.info( "server online session count：{}", sessionManager.getSessionCount() );
				try {
					TimeUnit.SECONDS.sleep( period );
				}
				catch ( InterruptedException e ) {
					// ignore
				}
				List<Session> sessions = sessionManager.getAllSession();
				for ( Session session : sessions ) {
					AbstractSession abstractSession = ( AbstractSession ) session;
					if ( abstractSession.isExpire() ) {
						if ( Duration.between( abstractSession.getLastKeepaliveTime(), LocalDateTime.now() ).getSeconds() < lose ) {
							// 提醒客户端发送心跳
							connector.keepaliveReq( abstractSession.getSessionId() );
						}
						else {
							connector.close( abstractSession.getSessionId(),
									AppMessage.builder().cmd( CmdEnum.CLOSE ).code( CodeEnum.KEEPALIVE_TIMEOUT ).build() );
						}
					}
				}
			}
		}
		catch ( Exception e ) {
			log.error( "OnlineListener running occur Throwable.", e );
		}
	}
}

```
* 心跳回复
    * 服务端收到客户端的心跳响应后
        * 服务端返回心跳响应
        * 记录客户端的最新一次心跳响应时间
```
package com.yijianguanzhu.transfer.server.handler;

import com.yijianguanzhu.transfer.common.utils.ContextUtil;
import com.yijianguanzhu.transfer.server.connector.AbstractConnector;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yijianguanzhu 2022年05月18日
 */
@Component
@ChannelHandler.Sharable
public class PingPongHandler extends ChannelDuplexHandler {

	@Autowired
	private AbstractConnector connector;

	@Override
	public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {
		if ( msg instanceof Message ) {
			AppMessage reqMsg = ( AppMessage ) msg;
			if ( reqMsg.getCmd() == CmdEnum.KEEPALIVE ) {
				connector.keepalive( ContextUtil.getChannelSessionHook( ctx ) );
				return;
			}
		}
		ctx.fireChannelRead( msg );
	}
}

```

* 账号登录处理
    * 账号登录成功后总体思路：
        * 首先检索当前节点是否已登录，有则踢下线。
        * 其次通过 `ClusterProxy` 推送到其他节点，如果账号已经登录其他节点，由其他节点通知账号下线。
        * 最后提醒本次登录成功。
```
package com.yijianguanzhu.transfer.auth.handler;

import com.yijianguanzhu.transfer.client.base.BaseResponseEntity;
import com.yijianguanzhu.transfer.client.feign.auth.AuthFeignClient;
import com.yijianguanzhu.transfer.client.result.UserResult;
import com.yijianguanzhu.transfer.cluster.enums.ClusterCmdEnum;
import com.yijianguanzhu.transfer.cluster.message.ClusterMessage;
import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.common.message.cluster.ClusterProxy;
import com.yijianguanzhu.transfer.common.props.TransferProperties;
import com.yijianguanzhu.transfer.common.provider.HandlerProvider;
import com.yijianguanzhu.transfer.common.utils.ContextUtil;
import com.yijianguanzhu.transfer.common.utils.JsonUtil;
import com.yijianguanzhu.transfer.common.utils.SpringUtil;
import com.yijianguanzhu.transfer.server.connector.AbstractConnector;
import com.yijianguanzhu.transfer.server.connector.session.AbstractSessionManager;
import com.yijianguanzhu.transfer.server.connector.session.Session;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import com.yijianguanzhu.transfer.server.message.AppMessage;
import com.yijianguanzhu.transfer.server.message.body.AuthBody;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

/**
 * 认证连接处理器
 *
 * @author yijianguanzhu 2022年05月18日
 */
@Slf4j
public class AuthHandler extends ChannelDuplexHandler implements HandlerProvider {

	@Override
	public HandlerProvider getInstance() {
		return new AuthHandler();
	}

	private final static String NOTICE_MESSAGE = "您的账号在另一地点登录";

	@Override
	public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {
		if ( msg instanceof Message ) {
			AppMessage reqMsg = ( AppMessage ) msg;
			if ( reqMsg.getCmd() == CmdEnum.LOGIN ) {
				String sessionHook = ContextUtil.getChannelSessionHook( ctx );
				AbstractConnector connector = SpringUtil.getBean( AbstractConnector.class );
				// 检查参数
				if ( StringUtils.isBlank( reqMsg.getBody() ) ) {
					connector.send( sessionHook, AppMessage.builder()
							.cmd( CmdEnum.ERROR ).code( CodeEnum.MISSING_BODY ).build() );
					return;
				}
				AuthBody body = JsonUtil.json2bean( reqMsg.getBody(), AuthBody.class );
				if ( StringUtils.isBlank( body.getToken() ) ) {
					connector.send( sessionHook, AppMessage.builder()
							.cmd( CmdEnum.ERROR ).code( CodeEnum.MISSING_TOKEN ).build() );
					return;
				}
				AuthFeignClient client = SpringUtil.getBean( AuthFeignClient.class );
				ResponseEntity<BaseResponseEntity<UserResult>> ping = client.ping( body.getToken() );
				if ( ping.getStatusCode() != HttpStatus.OK ) {
					connector.send( sessionHook, AppMessage.builder()
							.cmd( CmdEnum.ERROR ).code( CodeEnum.INVALID_TOKEN ).build() );
					return;
				}
				UserResult user = ping.getBody().getData();
				// 通知其他节点账号下线
				noticeNode( sessionHook, user );
				String userId = String.valueOf( user.getUserId() );
				AbstractSessionManager sessionManager = SpringUtil.getBean( AbstractSessionManager.class );
				Session session = sessionManager.getSession( sessionHook );
				// 设置sessionHook 为userId
				ContextUtil.setChannelSessionHook( ctx, String.valueOf( user.getUserId() ) );
				// 设置session认证状态
				session.setSessionId( userId );
				session.setAuthenticated( true );
				session.setAuthExpireTime( user.getExpireTime() );
				sessionManager.removeSession( sessionHook );
				sessionManager.addSession( session );
				// 登录成功消息
				connector.send( userId, AppMessage.builder()
						.cmd( CmdEnum.LOGIN ).code( CodeEnum.SUCCESS ).build() );
				return;
			}
		}
		ctx.fireChannelRead( msg );
	}

	private void noticeNode( String sessionHook, UserResult user ) {
		// 检查账号是否已经登录当前节点
		AbstractSessionManager sessionManager = SpringUtil.getBean( AbstractSessionManager.class );
		Session session = sessionManager.getSession( sessionHook );
		Session currentNodeSession = sessionManager.getSession( String.valueOf( user.getUserId() ) );
		if ( Objects.nonNull( currentNodeSession ) && currentNodeSession != session ) {
			// 通知当前节点账号下线
			AbstractConnector connector = SpringUtil.getBean( AbstractConnector.class );
			connector.close( String.valueOf( user.getUserId() ), AppMessage.builder()
					.cmd( CmdEnum.NOTICE ).code( CodeEnum.SUCCESS )
					.msg( NOTICE_MESSAGE ).build() );
		}
		TransferProperties properties = SpringUtil.getBean( TransferProperties.class );
		// 通知集群其他节点
		ClusterProxy.send( ClusterMessage.builder().clusterCmdEnum( ClusterCmdEnum.NOTICE )
				.msg( NOTICE_MESSAGE )
				.userId( user.getUserId() )
				.username( user.getUsername() )
				.instanceId( properties.getCluster().getInstanceId() ).build() );
		// 通知集群其他节点下线
		ClusterProxy.send( ClusterMessage.builder().clusterCmdEnum( ClusterCmdEnum.OFF )
				.msg( NOTICE_MESSAGE )
				.userId( user.getUserId() )
				.username( user.getUsername() )
				.instanceId( properties.getCluster().getInstanceId() ).build() );
	}

	@Override
	public int getOrder() {
		return 1;
	}
}

```

* 集群支持
```
package com.yijianguanzhu.transfer.cluster.rabbit;

/**
 * 使用direct直连交换机通信模型，消息只会被投入到routingKey一致的队列中
 *
 * @author yijianguanzhu 2022年06月02日
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "transfer-service.cluster.rabbit.enable", havingValue = "true")
@Import(RabbitAutoConfiguration.class)
class RabbitProducer implements ProducerProxy {

	@Autowired
	private RabbitProperties rabbitProperties;

	@Autowired
	private TransferProperties transferProperties;

	@Bean
	DirectExchange directExchange() {
		return new DirectExchange( rabbitProperties.getTemplate().getExchange(), true, false );
	}

	@Bean
	public Queue queue() {
		String queue = rabbitProperties.getTemplate().getDefaultReceiveQueue() + "_"
				+ transferProperties.getCluster().getInstanceId();
		// 消费者下线时，示意服务端自动删除队列
		return new Queue( queue, true, false, true );
	}

	@Bean
	Binding exchangeBinding( DirectExchange directExchange, Queue queue ) {
		return BindingBuilder.bind( queue ).to( directExchange )
				.with( rabbitProperties.getTemplate().getRoutingKey() );
	}

	@Bean
	MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public boolean produce( Object clusterMessage ) {
		rabbitTemplate.convertAndSend( clusterMessage );
		return true;
	}
}


============================================  分割线  ============================================

package com.yijianguanzhu.transfer.cluster.rabbit;

/**
 * @author yijianguanzhu 2022年06月02日
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "transfer-service.cluster.rabbit.enable", havingValue = "true")
class RabbitConsumer {

	@Autowired
	private TransferProperties transferProperties;

	@Autowired
	private AbstractConnector abstractConnector;

	@Autowired
	private Queue queue;

	@Autowired
	private MessageConverter messageConverter;

	// 注册消费者
	@Bean
	public SimpleMessageListenerContainer simpleMessageListenerContainer( ConnectionFactory connectionFactory ) {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer( connectionFactory );
		listenerContainer.setQueueNames( queue.getName() );
		listenerContainer.setMessageListener( this::receiveMessage );
		return listenerContainer;
	}

	public void receiveMessage( Message message ) {
		receiveMessage( ( ClusterMessage ) messageConverter.fromMessage( message ) );
	}

	public void receiveMessage( ClusterMessage message ) {
		log.info( "cluster message:{}", message );
		if ( !Objects.equals( transferProperties.getCluster().getInstanceId(), message.getInstanceId() ) ) {
			String sessionId = String.valueOf( message.getUserId() );
			if ( message.getClusterCmdEnum() == ClusterCmdEnum.NOTICE ) {
				abstractConnector.send( sessionId, AppMessage.builder()
						.cmd( CmdEnum.NOTICE ).code( CodeEnum.SUCCESS )
						.msg( message.getMsg() ).build() );
			}
			if ( message.getClusterCmdEnum() == ClusterCmdEnum.OFF ) {
				abstractConnector.close( sessionId );
			}
		}
	}
}
```

* 业务Handler支持横向扩展(SPI机制)
    * 总体思路（可参考 `AuthHandler` 类 ）：
        * 业务 handler 实现 `HandlerProvider` 接口
        * 根据排序需要，修改 `HandlerProvider.getOrder()` 返回值
        * 支持 `@Sharable` 注解，注入单例 handler
        

```

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


============================== 分割线 ==============================

package com.yijianguanzhu.transfer.server.config;

/**
 * @author yijianguanzhu 2022年05月18日
 */
@Component
@Slf4j
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    
	// 加载自定义handler组件
	List<HandlerProvider> handlerProviderList = new LinkedList<>();

	@PostConstruct
	private void loadingHandlerProvider() {
		ServiceLoader.load( HandlerProvider.class )
				.forEach( handlerProvider -> {
					Class<? extends HandlerProvider> providerClass = handlerProvider.getClass();
					boolean isSharable = providerClass.getAnnotation( Sharable.class ) != null;
					if ( isSharable && handlerProvider.getInstance() != handlerProvider ) {
						log.error( "This '{}' no needed to override the .getInstance() method as it's shared.", providerClass );
						return;
					}
					if ( !isSharable && handlerProvider.getInstance() == handlerProvider ) {
						log.error( "This '{}' need to override the .getInstance() method as it's not shared.", providerClass );
						return;
					}
					handlerProviderList.add( handlerProvider );
				} );
		this.handlerProviderList = handlerProviderList.stream().sorted( Comparator.comparing( HandlerProvider::getOrder ) )
				.collect( Collectors.toCollection( LinkedList::new ) );
	}

	@Override
	protected void initChannel( SocketChannel ch ) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		省略......
		handlerProviderList.forEach( handlerProvider -> pipeline.addLast( handlerProvider.getInstance() ) );
	}
}

```

### 配置文件
> application-dev.yml

    transfer-service:
      # 绑定端口号
      port: 8888
      session:
        # 单位/秒，客户端超过这个时间没回复心跳，就关闭连接
        keepalive-interval: 45
      time-wheel:
        # 单位/秒，多久检测一次客户端心跳
        keepalive-interval: 45
        # 单位/秒，客户端超过keepalive-interval这个时间没回复心跳，且没超过这个值，通知客户端发送心跳
        max-keepalive-interval: 120
      cluster:
        rabbit:
          enable: true
    # 使用原生RabbitMQ的配置
    spring:
      rabbitmq:
        host: 127.0.0.1
        port: 5672
        username: admin
        password: admin
        template:
          routing-key: websocket-transfer-routing-key
          exchange: websocket-transfer-exchange
          default-receive-queue: websocket-transfer-queue
    
> bootstrap.yml
    
    #nacos配置
    spring:
      cloud:
        nacos:
          # 注册中心地址
          discovery:
            server-addr: ${NACOS_ADDR:http://127.0.0.1:8848}
            username: ${NACOS_USERNAME:nacos}
            password: ${NACOS_PASSWORD:nacos}
          # 配置中心地址
          config:
            server-addr: ${NACOS_ADDR:http://127.0.0.1:8848}
            prefix: ${NACOS_CONFIG_PREFIX:icloud}
            file-extension: ${NACOS_CONFIG_FORMAT:yaml}
            username: ${NACOS_USERNAME:nacos}
            password: ${NACOS_PASSWORD:nacos}

### 程序运行示例
* 服务端启动
![服务端启动](https://user-images.githubusercontent.com/68835311/173513156-09774442-7ec4-4ac5-88c3-e1ba9a362686.png)
![服务端启动](https://user-images.githubusercontent.com/68835311/173513968-cb7ba883-5d93-4043-86c8-195cf1603dbf.png)

* 客户端启动
    * `token` 参考 [icloud-simple-service](https://github.com/yijianguanzhu/icloud-simple-service) 获取
    ![客户端启动](https://user-images.githubusercontent.com/68835311/173517355-cf78c05d-c99d-477a-a0bb-8717d6e02100.png)
