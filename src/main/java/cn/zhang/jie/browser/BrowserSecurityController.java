package cn.zhang.jie.browser;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import cn.zhang.jie.browser.support.SimpleResponse;
import cn.zhang.jie.browser.support.SocialUserInfo;
import cn.zhang.jie.core.properties.SercurityProperties;

//用来决定请求是否来自html，如果是跳转到登录页，否则返回json
@RestController
public class BrowserSecurityController {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private RequestCache requestCache = new HttpSessionRequestCache();
	//重定向工具
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	
	@Autowired
	private SercurityProperties sercurityProperties;
	//Spring提供的工具类
	@Autowired
	private ProviderSignInUtils providerSignInUtils; 
	
	//需要身份认证的时候跳转到这里
	@RequestMapping("/authentication/require")
	//自定义返回码
	@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
	public SimpleResponse requireAuthentication(HttpServletRequest request,HttpServletResponse response) throws IOException {
		//将引发跳转的请求保存下来
		SavedRequest savedRequest = requestCache.getRequest(request, response);
		if(savedRequest != null) {
			String targetUrl = savedRequest.getRedirectUrl();
			logger.info("引发跳转的url ：" + targetUrl);
			if(StringUtils.endsWithIgnoreCase(targetUrl, ".html")) {
				redirectStrategy.sendRedirect(request, response, sercurityProperties.getBrowser().getLoginPage());
			}
		}
		return new SimpleResponse("访问的服务需要身份认证，请引导用户到登录页");
	}
	
	@GetMapping("/social/user")
	public SocialUserInfo getSocialUserInfo(HttpServletRequest request) {
		SocialUserInfo userInfo = new SocialUserInfo();
		//从session中可以获取 connection，其中包含了用户信息
		Connection<?> connection = providerSignInUtils.getConnectionFromSession(new ServletWebRequest(request));
		userInfo.setProviderId(connection.getKey().getProviderId());
		userInfo.setProviderUserId(connection.getKey().getProviderUserId());
		userInfo.setNickName(connection.getDisplayName());
		userInfo.setHeadimg(connection.getImageUrl());
		return userInfo;
	}
}
