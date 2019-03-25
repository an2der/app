package com.highteam.router.oauth2.model;

import java.io.Serializable;

public class CustomOAuth2AuthenticationInfo implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2344890528630271159L;

	private String loginName;
	private String password;
	private String authType;
	private String clientIp;
	private String equipinfo;
	private String userAccount;
	private int serviceType;


	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}
	
	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getEquipinfo() {
		return equipinfo;
	}

	public void setEquipinfo(String equipinfo) {
		this.equipinfo = equipinfo;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public int getServiceType() {
		return serviceType;
	}

	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}
}
