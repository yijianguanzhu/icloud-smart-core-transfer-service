package com.yijianguanzhu.transfer.client.result;

import lombok.*;

import java.time.LocalDateTime;

/**
 * @author yijianguanzhu 2022年06月10日
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserResult {

	private Long userId;

	private String username;

	/**
	 * 过期时间
	 */
	@Builder.Default
	private LocalDateTime expireTime = LocalDateTime.now().plusMonths( 1L );
}
