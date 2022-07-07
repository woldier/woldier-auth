package com.woldier.auth.auth.utils;

import com.woldier.auth.context.BaseContextConstants;
import com.woldier.auth.exception.BizException;
import com.woldier.auth.exception.code.ExceptionCode;
import com.woldier.auth.utils.DateUtils;
import com.woldier.auth.utils.NumberHelper;
import com.woldier.auth.utils.StrHelper;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;

/**
 * Jwt生成帮助类
 */
@Slf4j
public class JwtHelper {
    private static final RsaKeyHelper RSA_KEY_HELPER = new RsaKeyHelper();
    /**
     * 生成用户token
     * @param jwtInfo jwt信息
     * @param priKeyPath 私钥
     * @param expire 公钥
     * @return
     * @throws BizException
     */
    public static Token generateUserToken(JwtUserInfo jwtInfo, String priKeyPath, int expire) throws BizException {
        JwtBuilder jwtBuilder = Jwts.builder()
                //设置主题  建议去看一下源码
                .setSubject(String.valueOf(jwtInfo.getUserId()))
                .claim(BaseContextConstants.JWT_KEY_ACCOUNT, jwtInfo.getAccount())
                .claim(BaseContextConstants.JWT_KEY_NAME, jwtInfo.getName())
                .claim(BaseContextConstants.JWT_KEY_ORG_ID, jwtInfo.getOrgId())
                .claim(BaseContextConstants.JWT_KEY_STATION_ID, jwtInfo.getStationId());

        return generateToken(jwtBuilder, priKeyPath, expire);
    }

    /**
     * 获取token中的用户信息
     * @param token      token
     * @param pubKeyPath 公钥路径
     * @return
     * @throws Exception
     */
    public static JwtUserInfo getJwtFromToken(String token, String pubKeyPath) throws BizException {
        /*token解析*/
        Jws<Claims> claimsJws = parserToken(token, pubKeyPath);
        /*返回token体*/
        Claims body = claimsJws.getBody();
        /*返回子主题，不存在则返回空*/
        String strUserId = body.getSubject();
        /*获取account*/
        String account = StrHelper.getObjectValue(body.get(BaseContextConstants.JWT_KEY_ACCOUNT));
        /*获取name*/
        String name = StrHelper.getObjectValue(body.get(BaseContextConstants.JWT_KEY_NAME));
        /*获取orgid*/
        String strOrgId = StrHelper.getObjectValue(body.get(BaseContextConstants.JWT_KEY_ORG_ID));
        /*获取stationid*/
        String strDepartmentId = StrHelper.getObjectValue(body.get(BaseContextConstants.JWT_KEY_STATION_ID));
        /*转为long*/
        Long userId = NumberHelper.longValueOf0(strUserId);
        Long orgId = NumberHelper.longValueOf0(strOrgId);
        Long departmentId = NumberHelper.longValueOf0(strDepartmentId);
        return new JwtUserInfo(userId, account, name, orgId, departmentId);
    }

    /**
     * 生成token
     * @param builder
     * @param priKeyPath
     * @param expire
     * @return
     * @throws BizException
     */
    protected static Token generateToken(JwtBuilder builder, String priKeyPath, int expire) throws BizException {
        try {
            //返回的字符串便是我们的jwt串了
            String compactJws = builder.
                    /*设置过期时间 先获取现在的时间然后根据expore得到延时后的时间 LocalDateTime转为Date*/
                    setExpiration(DateUtils.localDateTime2Date(LocalDateTime.now().plusSeconds(expire)))
                    //设置算法（必须），私有key通过JwtHelper.RSA_KEY_HELPER对象的getPrivateKey方法获得
                    .signWith(SignatureAlgorithm.RS256, RSA_KEY_HELPER.getPrivateKey(priKeyPath))
                    //这个是全部设置完成后拼成jwt串的方法
                    .compact();
            return new Token(compactJws, expire);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("errcode:{}, message:{}", ExceptionCode.JWT_GEN_TOKEN_FAIL.getCode(), e.getMessage());
            throw new BizException(ExceptionCode.JWT_GEN_TOKEN_FAIL.getCode(), ExceptionCode.JWT_GEN_TOKEN_FAIL.getMsg());
        }
    }

    /**
     * 公钥解析token
     * @param token
     * @param pubKeyPath 公钥路径
     * @return
     * @throws Exception
     */
    private static Jws<Claims> parserToken(String token, String pubKeyPath) throws BizException {
        try {
            return Jwts.parser()
                    .setSigningKey( //设置公钥
                            RSA_KEY_HELPER.getPublicKey(pubKeyPath)
                    )
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException ex) {
            //过期
            throw new BizException(
                    /*通过定义的ExceptionCode枚举类选定code以及msg  ----下同*/
                    ExceptionCode.JWT_TOKEN_EXPIRED.getCode(), ExceptionCode.JWT_TOKEN_EXPIRED.getMsg()
            );
        } catch (SignatureException ex) {
            //签名错误
            throw new BizException(
                    ExceptionCode.JWT_SIGNATURE.getCode(), ExceptionCode.JWT_SIGNATURE.getMsg()
            );
        } catch (IllegalArgumentException ex) {
            //token 为空
            throw new BizException(
                    ExceptionCode.JWT_ILLEGAL_ARGUMENT.getCode(), ExceptionCode.JWT_ILLEGAL_ARGUMENT.getMsg()
            );
        } catch (Exception e) {
            /*若以上都有问题 则说明token解析出了问题*/
            log.error("errcode:{}, message:{}", ExceptionCode.JWT_PARSER_TOKEN_FAIL.getCode(), e.getMessage());
            throw new BizException(ExceptionCode.JWT_PARSER_TOKEN_FAIL.getCode(), ExceptionCode.JWT_PARSER_TOKEN_FAIL.getMsg());
        }
    }
}
