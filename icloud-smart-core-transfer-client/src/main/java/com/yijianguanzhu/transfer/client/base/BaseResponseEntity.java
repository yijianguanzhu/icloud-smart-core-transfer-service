package com.yijianguanzhu.transfer.client.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * @author yijianguanzhu 2022年06月10日
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponseEntity<T> {

	private Integer code;

	private String msg;

	private T data;
}
