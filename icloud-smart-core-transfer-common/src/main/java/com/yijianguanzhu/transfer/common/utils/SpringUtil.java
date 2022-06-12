package com.yijianguanzhu.transfer.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author yijianguanzhu 2022年06月10日
 */
@Component
public class SpringUtil implements ApplicationContextAware {

	private static ApplicationContext context;

	@Override
	public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
		SpringUtil.context = applicationContext;
	}

	public static <T> T getBean( Class<T> clazz ) {
		if ( clazz == null ) {
			return null;
		}
		return context.getBean( clazz );
	}

	public static <T> T getBean( String beanId ) {
		if ( beanId == null ) {
			return null;
		}
		return ( T ) context.getBean( beanId );
	}

	public static <T> T getBean( String beanName, Class<T> clazz ) {
		if ( null == beanName || "".equals( beanName.trim() ) ) {
			return null;
		}
		if ( clazz == null ) {
			return null;
		}
		return ( T ) context.getBean( beanName, clazz );
	}

	public static ApplicationContext getContext() {
		if ( context == null ) {
			return null;
		}
		return context;
	}
}
