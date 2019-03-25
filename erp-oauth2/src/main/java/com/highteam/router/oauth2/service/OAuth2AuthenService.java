package com.highteam.router.oauth2.service;


import com.highteam.router.oauth2.model.CustomOAuth2AuthenticationInfo;

public interface OAuth2AuthenService {

	Object auth(CustomOAuth2AuthenticationInfo info);
}
