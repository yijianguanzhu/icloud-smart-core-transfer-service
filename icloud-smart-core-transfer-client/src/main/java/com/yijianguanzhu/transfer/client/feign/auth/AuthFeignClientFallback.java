package com.yijianguanzhu.transfer.client.feign.auth;

import com.yijianguanzhu.transfer.client.api.R;
import com.yijianguanzhu.transfer.client.base.BaseResponseEntity;
import com.yijianguanzhu.transfer.client.result.UserResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author yijianguanzhu 2022年06月10日
 */
@Component
public class AuthFeignClientFallback implements AuthFeignClient {

	@Override
	public ResponseEntity<BaseResponseEntity<UserResult>> ping( String token ) {
		return R.fail( "token无效" );
	}
}
