package com.yijianguanzhu.transfer.server.message.body;

import lombok.*;

/**
 * @author yijianguanzhu 2022年06月10日
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthBody {

	private String token;
}
