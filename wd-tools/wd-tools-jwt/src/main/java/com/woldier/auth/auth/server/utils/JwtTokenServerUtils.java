package com.woldier.auth.auth.server.utils;

import com.woldier.auth.auth.server.properties.AuthServerProperties;
import com.woldier.auth.auth.utils.JwtHelper;
import com.woldier.auth.auth.utils.JwtUserInfo;
import com.woldier.auth.auth.utils.Token;
import com.woldier.auth.exception.BizException;
import lombok.AllArgsConstructor;
/**
 * jwt token 工具
 *
 */
@AllArgsConstructor
public class JwtTokenServerUtils {
    /**
     * 认证服务端使用，如 authority-server
     * 生成和 解析token
     */
    private AuthServerProperties authServerProperties;

    /**
     * 生成token
     * @param jwtInfo
     * @param expire
     * @return
     * @throws BizException  更具返回的 BizException中的code 再全局异常处理器中进行处理
     */
    public Token generateUserToken(JwtUserInfo jwtInfo, Integer expire) throws BizException {
        /*得到AuthServerProperties中的内嵌类TokenInfo的对象并为他赋值（其值源于属性类中的TokenInfo对象）*/
        AuthServerProperties.TokenInfo userTokenInfo = authServerProperties.getUser();
        if (expire == null || expire <= 0) {
            /*若参数expire为null 或者小于等于零 则使用默认的expire*/
            expire = userTokenInfo.getExpire();
        }
        return JwtHelper.generateUserToken(jwtInfo, userTokenInfo.getPriKey(), expire);
    }

    /**
     * 解析token
     * @param token 传入token
     * @throws BizException 更具返回的 BizException中的code 再全局异常处理器中进行处理
     */
    public JwtUserInfo getUserInfo(String token) throws BizException {
        AuthServerProperties.TokenInfo userTokenInfo = authServerProperties.getUser();
        return JwtHelper.getJwtFromToken(token, userTokenInfo.getPubKey());
    }
}
