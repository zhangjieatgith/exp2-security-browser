package cn.zhang.jie.browser;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.social.security.SpringSocialConfigurer;

import cn.zhang.jie.browser.authentication.ImoocAuthenticationFailHandler;
import cn.zhang.jie.browser.authentication.ImoocAuthenticationSuccessHandler;
import cn.zhang.jie.browser.session.ImoocExpiredSessionStrategy;
import cn.zhang.jie.core.authentication.mobile.SmsCodeAuthenticationSecurityConfig;
import cn.zhang.jie.core.properties.SercurityProperties;
import cn.zhang.jie.core.validate.code.SmsCodeFilter;
import cn.zhang.jie.core.validate.code.ValidateCodeFilter;

@Configuration
public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	private SercurityProperties sercurityProperties;
	@Autowired
	private ImoocAuthenticationSuccessHandler imoocAuthenticationSuccessHandler;
	@Autowired
	private ImoocAuthenticationFailHandler imoocAuthenticationFailHandler; 
	@Autowired
	//导入和短信验证相关的配置
	private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig; 
	
	@Autowired
	//这是我们自己的实现类，这里用作“记住我”之后，根据用户名获取用户信息 
	private UserDetailsService userDetailsService; 
	
	@Autowired
	private SpringSocialConfigurer imoocSocialSecurityConfig;
	
	@Bean
	//加上该配置后，就启用了密码加解密的功能
	public PasswordEncoder passwordEncoder() {
		//这里也可以返回自定义的加解密，比如 md5的方式
		return new BCryptPasswordEncoder();
	} 
	
	@Autowired
	private DataSource dataSource;
	
	@Bean
	//该配置用于“记住我”功能
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl tokenRepositoryImpl = new JdbcTokenRepositoryImpl();
		tokenRepositoryImpl.setDataSource(dataSource);
		//系统启动的时候创建表
		tokenRepositoryImpl.setCreateTableOnStartup(false);
		return tokenRepositoryImpl;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//这是自定义的用来出来图形验证码校验的过滤器
		ValidateCodeFilter validateCodeFilter = new ValidateCodeFilter();
		validateCodeFilter.setAuthenticationFailureHandler(imoocAuthenticationFailHandler);
		//将配置信息传递给 validateCodeFilter，并调用初始化方法afterPropertiesSet
		validateCodeFilter.setSercurityProperties(sercurityProperties);
		validateCodeFilter.afterPropertiesSet();
		
		
		//这是自定义的用来出来短信验证码校验的过滤器
		SmsCodeFilter smsCodeFilter = new SmsCodeFilter();
		smsCodeFilter.setAuthenticationFailureHandler(imoocAuthenticationFailHandler);
		smsCodeFilter.setSercurityProperties(sercurityProperties);
		smsCodeFilter.afterPropertiesSet();
		
		
		//在过滤器链中配置自定义的过滤器，这里是用来添加图形校验码的校验逻辑
		http.addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class)
			//表示：如果是表单登录，那么就使用下面的配置
			.formLogin()
			//自定义登录页
			.loginPage("/authentication/require")
			//默认使用 /login来请求认证的，这里修改为自定义的url来请求认证
			.loginProcessingUrl("/authentication/form")
			//使用自定义的登录成功处理器
			.successHandler(imoocAuthenticationSuccessHandler)
			//使用自定义的登录失败处理器
			.failureHandler(imoocAuthenticationFailHandler)
			//“记住我”的配置
			.and().rememberMe()
			//“记住我”的tokenRepository
			.tokenRepository(persistentTokenRepository())
			//“记住我”的过期时间
			.tokenValiditySeconds(sercurityProperties.getBrowser().getRememberMeSeconds())
			//“记住我”在拿到用户名之后，用哪个实现去获取用户详细信息
			.userDetailsService(userDetailsService)
			//session管理
			.and().sessionManagement()
			//当session失效的时候的跳转地址,通常应该跳转到登录页
			.invalidSessionUrl("/session/invalid")
			//配置最大的session数量，用于保证session并发控制，1表示同一个用户，在后面产生的session会将前面产生的session覆盖掉
			.maximumSessions(1)
			//表示如果在Chrome中已经登录，此时又在Firefox中登录时，将被拒绝。即当session的数量达到最大时，将阻止后续的登录行为
			.maxSessionsPreventsLogin(true)
			//新的session覆盖掉旧的session，如果需要记录一些信息，比如哪个session在什么时候将旧的session替换掉了，需要实现下面的接口
			.expiredSessionStrategy(new ImoocExpiredSessionStrategy())
			.and()
		//表示使用 httpBasic 做验证
		//http.httpBasic()
			//开启请求的授权
			.and().authorizeRequests()
			//表示针对某些页面，赋予所有权限
			.antMatchers("/authentication/require","/image/code","/code/*","/user/register","/session/invalid"
				,sercurityProperties.getBrowser().getLoginPage(), sercurityProperties.getBrowser().getSignUpUrl()).permitAll()
			//表示针对的是任意请求
			.anyRequest()
			//都需要身份认证
			.authenticated()
			//将跨站请求伪造禁用
			.and().csrf().disable()
			//将 smsCodeAuthenticationSecurityConfig 中的配置也加入到当前的 http 中
			.apply(smsCodeAuthenticationSecurityConfig)
			//作用是向当前的过滤器链上添加一个过滤器，引导用户做社交登录
			.and().apply(imoocSocialSecurityConfig);		
	}
}
