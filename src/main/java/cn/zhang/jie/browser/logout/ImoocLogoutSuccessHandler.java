package cn.zhang.jie.browser.logout;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.zhang.jie.browser.support.SimpleResponse;
import cn.zhang.jie.core.properties.SercurityProperties;

//退出成功后会调用这里的方法
public class ImoocLogoutSuccessHandler implements LogoutSuccessHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private SercurityProperties sercurityProperties; 
	
	public ImoocLogoutSuccessHandler(SercurityProperties sercurityProperties) {
		this.sercurityProperties = sercurityProperties;
	}
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		//比如记录退出日志等
		logger.info("退出成功");
		if(StringUtils.isBlank(sercurityProperties.getBrowser().getSignOutUrl())) {
			//如果没有配置退出页面，返回json
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(objectMapper.writeValueAsString(new SimpleResponse("退出成功")));
		} else {
			//否则就重定向到指定url
			response.sendRedirect(sercurityProperties.getBrowser().getSignOutUrl());
		}
	}
}
