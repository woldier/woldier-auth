package com.woldier.auth.service.service.impl;

import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.base.Captcha;
import com.woldier.auth.authority.dto.auth.LoginDTO;
import com.woldier.auth.authority.dto.auth.LoginParamDTO;
import com.woldier.auth.common.constant.CacheKey;
import com.woldier.auth.exception.BizException;
import com.woldier.auth.service.service.LoginValidateService;

import lombok.val;
import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.sun.corba.se.impl.util.RepositoryId.cache;

@Service
public class LoginValidateServiceImpl implements LoginValidateService {


    @Autowired
    private CacheChannel cacheChannel;
    @Override
    public void captcha(String key, HttpServletResponse response) throws IOException {
        /* 生成验证码
                * 保存验证码到cache
                * 生成的图片流返回给用户页面
                */


        if (StringUtils.isBlank(key)) {
            throw BizException.validFail("验证码key不能为空");
        }

        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        response.setHeader(HttpHeaders.PRAGMA, "No-cache");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "No-cache");
        response.setDateHeader(HttpHeaders.EXPIRES, 0L);

        Captcha captcha = new ArithmeticCaptcha(115, 42);
        captcha.setCharType(2);

        cacheChannel.set(CacheKey.CAPTCHA, key, StringUtils.lowerCase(captcha.text()));
        captcha.out(response.getOutputStream());
    }


    @Override
    public void login(LoginParamDTO loginParamDTO) {

        // 验证码校验
        CacheObject cacheObject = cacheChannel.get( CacheKey.CAPTCHA,loginParamDTO.getKey());
        if(cacheObject.getValue()==null)
            throw new BizException("验证码已经过期");
        if(!cacheObject.getValue().equals(loginParamDTO.getCode()))
            throw new BizException("验证码不匹配");
        /*验证码清除*/
        //cacheChannel.evict(CacheKey.CAPTCHA,loginParamDTO.getKey());
    }
}
