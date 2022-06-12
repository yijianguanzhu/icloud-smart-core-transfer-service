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
