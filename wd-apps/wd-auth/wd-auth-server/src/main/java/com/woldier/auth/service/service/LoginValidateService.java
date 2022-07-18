package com.woldier.auth.service.service;

import com.woldier.auth.authority.dto.auth.LoginDTO;
import com.woldier.auth.authority.dto.auth.LoginParamDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface LoginValidateService {


    /**
     * 生成验证码
     * 保存验证码到cache
     * 生成的图片流返回给用户页面
     * @param key
     * @param response
     */
    public void captcha(String key , HttpServletResponse response) throws IOException;

    /**
     * 用户登陆
     * @param loginParamDTO
     * @return
     */
    public void login (LoginParamDTO loginParamDTO);
}
