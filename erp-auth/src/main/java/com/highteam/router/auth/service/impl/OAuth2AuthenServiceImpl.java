package com.highteam.router.auth.service.impl;

import com.highteam.router.m.UserInfo;
import com.highteam.router.oauth2.model.CustomOAuth2AuthenticationInfo;
import com.highteam.router.oauth2.service.OAuth2AuthenService;
import org.springframework.stereotype.Service;


@Service
public class OAuth2AuthenServiceImpl implements OAuth2AuthenService {

    @Override
    public Object auth(CustomOAuth2AuthenticationInfo info) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(1);
        return userInfo;
    }

}
