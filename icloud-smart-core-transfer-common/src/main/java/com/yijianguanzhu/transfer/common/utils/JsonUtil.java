package com.yijianguanzhu.transfer.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

/**
 * @author yijianguanzhu 2022年05月17日
 */
public final class JsonUtil {

	public final static ObjectMapper JACKSONMAPPER = new ObjectMapper();

	static {
		JACKSONMAPPER.setSerializationInclusion( JsonInclude.Include.NON_NULL );
		JACKSONMAPPER.configure( DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false );
		JACKSONMAPPER.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
		JACKSONMAPPER.configure( JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true );
	}

	@SneakyThrows
	public static <T> T json2bean( String jsonString, Class<T> clazz ) {
		return JACKSONMAPPER.readValue( jsonString, clazz );
	}

	@SneakyThrows
	public static <T> String bean2json( Object bean ) {
		return JACKSONMAPPER.writeValueAsString( bean );
	}

	@SneakyThrows
	public static <T> byte[] bean2byte( Object bean ) {
		return JACKSONMAPPER.writeValueAsBytes( bean );
	}

	@SneakyThrows
	public static <T> T byte2bean( byte[] array, Class<T> clazz ) {
		return JACKSONMAPPER.readValue( array, clazz );
	}
}
