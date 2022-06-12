package com.yijianguanzhu.transfer.cluster.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author yijianguanzhu 2022年06月10日
 */
@AllArgsConstructor
@Getter
public enum ClusterCmdEnum {

	NOTICE( "notice", "发送给其他节点账号的消息" ),

	OFF( "off", "通知其他节点账号下线" ),
	;
	@JsonValue
	private String cmd;

	private String desc;

	@JsonCreator
	public static ClusterCmdEnum from( String cmd ) {
		ClusterCmdEnum[] values = ClusterCmdEnum.values();
		for ( ClusterCmdEnum e : values ) {
			if ( Objects.equals( cmd, e.getCmd() ) )
				return e;
		}
		return null;
	}
}
