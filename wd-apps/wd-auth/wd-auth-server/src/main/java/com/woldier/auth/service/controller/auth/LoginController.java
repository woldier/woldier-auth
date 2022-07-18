package com.woldier.auth.service.controller.auth;

import com.woldier.auth.authority.dto.auth.LoginDTO;
import com.woldier.auth.authority.dto.auth.LoginParamDTO;
import com.woldier.auth.base.R;
import com.woldier.auth.service.service.AuthManager;
import com.woldier.auth.service.service.LoginValidateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * 用户登陆的控制器
 */
@RestController
@ResponseBody
@Api(tags = "用户登陆组件",value = "LoginController")
@RequestMapping("/anno")
@Validated //开启校验功能
public class LoginController {

    @Autowired
    private LoginValidateService loginValidateService;

    @Autowired
    private AuthManager authManager;

    /**
     * 验证码的生成
     */
    @GetMapping(value = "/captcha",produces = "image/png")
    @ApiOperation(notes = "验证码",value = "验证码")
    public void captcha(@RequestParam("key") @NotBlank(message = "不能为空") String key, HttpServletResponse response) throws IOException {
        loginValidateService.captcha(key,response);
    }

    @ApiOperation(value = "用户登陆",notes = "用户输入用户名密码，并传递用于校验验证码的key")
    @PostMapping("/login")
    public R<LoginDTO> login(@RequestBody @Validated LoginParamDTO loginParamDTO) {
        /*验证码的校验*/

        loginValidateService.login(loginParamDTO);
        /*没有抛出一场说明验证码验证成功，进行登陆操作*/
        val loginDTO = authManager.login(loginParamDTO);


        return R.success(loginDTO);
    }
}
