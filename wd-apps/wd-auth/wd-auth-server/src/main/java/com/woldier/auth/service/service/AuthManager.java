package com.woldier.auth.service.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.dozermapper.core.Mapper;
import com.woldier.auth.auth.client.properties.AuthClientProperties;
import com.woldier.auth.auth.server.utils.JwtTokenServerUtils;
import com.woldier.auth.auth.utils.JwtUserInfo;
import com.woldier.auth.auth.utils.Token;
import com.woldier.auth.authority.dto.auth.LoginDTO;
import com.woldier.auth.authority.dto.auth.LoginParamDTO;
import com.woldier.auth.authority.dto.auth.ResourceQueryDTO;
import com.woldier.auth.authority.dto.auth.UserDTO;
import com.woldier.auth.authority.entity.auth.Resource;
import com.woldier.auth.authority.entity.auth.User;
import com.woldier.auth.base.R;
import com.woldier.auth.common.constant.CacheKey;
import com.woldier.auth.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.oschina.j2cache.CacheChannel;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.Digest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限认证管理器
 */
@Service
@Slf4j
public class AuthManager {
    @Autowired
    private UserService userService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private JwtTokenServerUtils jwtTokenServerUtils;

    @Autowired
    private CacheChannel cacheChannel;
    @Autowired
    private Mapper mapper;

    /**
     * 登陆
     * @param loginParamDTO
     * @return
     */
    public LoginDTO login(LoginParamDTO loginParamDTO) {
        /*用户登陆密码验证*/
        val user = loginUserCheck(loginParamDTO);
        UserDTO userDTO = new UserDTO();
        /*dozer映射*/
        mapper.map(user,userDTO);
        /*jwt令牌*/
        val token = generateUserToken(user);

        /*获取userResource信息*/
        val resource = resourceService.findVisibleResource(ResourceQueryDTO.builder().userId(userDTO.getId()).build());
        //log.info(resource.toString());
        List<String> collect1=null;
        if(resource!=null&&resource.size()>0) {
            /*缓存前段数据--前端只需要里面的code字段*/
            collect1 = resource.stream().map(Resource::getCode).collect(Collectors.toList());
            /*前段数据返回给前端通过localstorage缓存*/
            /*缓存后端数据*/
            List<String> collect2 = resource.stream().map(resource1 -> resource1.getMethod() + resource1.getUrl()).collect(Collectors.toList());
            cacheChannel.set(CacheKey.RESOURCE,userDTO.getAccount(),collect1);

        }
        LoginDTO loginDTO = LoginDTO.builder()
                .user(userDTO)
                .token(token)
                .permissionsList(collect1)
                .build();
        return loginDTO;
    }

    private Token generateUserToken(User user) {
        JwtUserInfo jwtUserInfo = new JwtUserInfo(user.getId(), user.getAccount(), user.getName(), user.getOrgId(), user.getStationId());

        Token token = jwtTokenServerUtils.generateUserToken(jwtUserInfo, null);
        return token;
    }

    /**
     * 用户登陆的密码验证
     * @param loginParamDTO
     */
    private User loginUserCheck(LoginParamDTO loginParamDTO) {
        /*根据account查询数据库*/
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(User::getAccount, loginParamDTO.getAccount());
        User user = userService.getOne(lambdaQueryWrapper);
        if(user==null)
            throw new BizException("用户名错误");

        /*密码md5比对*/
        //val passwordMd5 = DigestUtils.md5Hex(user.getPassword());
        if(!loginParamDTO.getPassword().equals(loginParamDTO.getPassword()))
            throw new BizException("密码错误");

        return user;
    }
}
