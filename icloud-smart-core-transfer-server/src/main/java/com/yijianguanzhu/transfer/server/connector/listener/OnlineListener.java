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

	private void await() {
		if ( lock.tryLock() ) {
			try {
				notEmpty.await();
			}
			catch ( Exception e ) {
				// ignore
			}
			finally {
				lock.unlock();
			}
		}
	}

	public void signal() {
		if ( lock.tryLock() ) {
			try {
				notEmpty.signalAll();
			}
			catch ( Exception e ) {
				// ignore
			}
			finally {
				lock.unlock();
			}
		}
	}
}
