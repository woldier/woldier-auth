package com.woldier.auth.service.config;

import com.woldier.auth.common.handler.DefaultGlobalExceptionHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {Controller.class, RestController.class})
@ResponseBody
public class ServiceGlobalExceptionHandler extends DefaultGlobalExceptionHandler {

}
