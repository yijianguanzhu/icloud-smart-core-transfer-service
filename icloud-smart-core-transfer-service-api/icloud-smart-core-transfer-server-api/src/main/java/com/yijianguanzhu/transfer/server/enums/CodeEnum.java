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
public enum CodeEnum {

	SUCCESS( 0, null ),

	KEEPALIVE_TIMEOUT( 1001, "keepalive timeout" ),

	ERR_TEXT_FRAME( 10010, "invalid text frame data source" ),

	ERR_BINARY_FRAME( 10011, "invalid binary frame data source" ),

	MISSING_BODY( 10012, "missing body" ),

	MISSING_TOKEN( 10013, "missing token" ),

	INVALID_TOKEN( 10014, "invalid token" ),

	CLOSE( 51, "close connect" );

	@JsonValue
	private int code;

	private String msg;

	@JsonCreator
	public static CodeEnum from( int code ) {
		CodeEnum[] values = CodeEnum.values();
		for ( CodeEnum e : values ) {
			if ( Objects.equals( code, e.getCode() ) )
				return e;
		}
		return null;
	}
}
