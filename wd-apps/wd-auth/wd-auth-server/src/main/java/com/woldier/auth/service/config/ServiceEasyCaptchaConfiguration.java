package com.woldier.auth.service.config;

import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceEasyCaptchaConfiguration {

    public Captcha getCaptcha(){
        return new ArithmeticCaptcha(200,30);
    }
}
