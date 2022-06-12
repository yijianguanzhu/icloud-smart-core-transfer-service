package com.yijianguanzhu.transfer.client.feign.auth;

import com.yijianguanzhu.transfer.client.base.BaseResponseEntity;
import com.yijianguanzhu.transfer.client.result.UserResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.yijianguanzhu.transfer.client.constant.TokenConstant;

/**
 * 授权模块feign client
 *
 * @author yijianguanzhu 2022年06月10日
 */
@FeignClient(value = "gateway", contextId = "auth", fallback = AuthFeignClientFallback.class)
public interface AuthFeignClient {

	/**
	 * 获取账号信息
	 */
	@RequestMapping(value = "/auth/ping", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<BaseResponseEntity<UserResult>> ping( @RequestHeader(TokenConstant.TOKEN) String token );
}
