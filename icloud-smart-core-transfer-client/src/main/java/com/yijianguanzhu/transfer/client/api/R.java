package com.yijianguanzhu.transfer.client.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.yijianguanzhu.transfer.client.base.BaseResponseEntity;

/**
 * @author yijianguanzhu 2022年06月10日
 */
public class R {

	public static <T> ResponseEntity<BaseResponseEntity<T>> data( T data ) {
		return data( data, null );
	}

	public static <T> ResponseEntity<BaseResponseEntity<T>> data( T data, String msg ) {
		return data( 0, data, msg );
	}

	public static <T> ResponseEntity<BaseResponseEntity<T>> data( int code, T data ) {
		return data( code, data, null );
	}

	public static <T> ResponseEntity<BaseResponseEntity<T>> data( int code, T data, String msg ) {
		return result( HttpStatus.OK, code, data, msg );
	}

	public static <T> ResponseEntity<BaseResponseEntity<T>> success( String msg ) {
		return data( null, msg );
	}

	public static <T> ResponseEntity<BaseResponseEntity<T>> success( int code, String msg ) {
		return data( code, null, msg );
	}

	public static <T> ResponseEntity<BaseResponseEntity<T>> fail( String msg ) {
		return fail( HttpStatus.BAD_REQUEST, msg );
	}

	public static <T> ResponseEntity<BaseResponseEntity<T>> fail( int code, String msg ) {
		return fail( HttpStatus.BAD_REQUEST, code, msg );
	}

	public static <T> ResponseEntity<BaseResponseEntity<T>> fail( HttpStatus status, String msg ) {
		return fail( status, -1, msg );
	}

	public static <T> ResponseEntity<BaseResponseEntity<T>> fail( HttpStatus status, int code, String msg ) {
		return result( status, code, null, msg );
	}

	// 响应结果
	private static <T> ResponseEntity<BaseResponseEntity<T>> result( HttpStatus status, int code, T data, String msg ) {
		return ResponseEntity
				.status( status )
				.body( BaseResponseEntity.<T>builder()
						.code( code )
						.data( data )
						.msg( msg )
						.build() );
	}
}
