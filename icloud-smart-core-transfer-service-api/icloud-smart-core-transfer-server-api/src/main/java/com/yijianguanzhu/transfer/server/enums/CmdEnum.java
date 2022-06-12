package com.yijianguanzhu.transfer.server.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author yijianguanzhu 2022年05月17日
 */
@AllArgsConstructor
@Getter
public enum CmdEnum {

	UNKNOWN( "unknown", "未知的" ),

	KEEPALIVE_REQ( "keepalive_req", "心跳请求" ),

	KEEPALIVE( "keepalive", "心跳响应" ),

	LOGIN( "login", "通道登录" ),

	CLOSE( "close", "关闭连接" ),

	NOTICE( "notice", "通知" ),

	ERROR( "error", "错误的数据" );

	@JsonValue
	private String cmd;

	private String desc;

	@JsonCreator
	public static CmdEnum from( String cmd ) {
		CmdEnum[] values = CmdEnum.values();
		for ( CmdEnum e : values ) {
			if ( Objects.equals( cmd, e.getCmd() ) )
				return e;
		}
		return UNKNOWN;
	}
}
