package cn.zhang.jie.browser.support;

public class SocialUserInfo {

	//哪个服务提供商
	private String providerId;
	//服务提供商中的userid,即 openid
	private String providerUserId;
	//用户昵称
	private String nickName;
	//用户头像
	private String headimg;
	
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public String getProviderUserId() {
		return providerUserId;
	}
	public void setProviderUserId(String providerUserId) {
		this.providerUserId = providerUserId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getHeadimg() {
		return headimg;
	}
	public void setHeadimg(String headimg) {
		this.headimg = headimg;
	}
}
