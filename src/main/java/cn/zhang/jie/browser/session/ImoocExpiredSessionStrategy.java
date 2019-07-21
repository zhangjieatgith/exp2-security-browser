package cn.zhang.jie.browser.session;

import java.io.IOException;

import javax.servlet.ServletException;

import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

//如果新的session替换掉旧的session，可以在这里记录一些信息
public class ImoocExpiredSessionStrategy implements SessionInformationExpiredStrategy {

	@Override
	public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
		event.getResponse().setContentType("application/json;charset=UTF-8");
		event.getResponse().getWriter().write("并发登录!");
	}
}
