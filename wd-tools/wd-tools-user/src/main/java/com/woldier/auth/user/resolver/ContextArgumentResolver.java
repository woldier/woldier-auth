package com.woldier.auth.user.resolver;

import com.woldier.auth.base.R;
import com.woldier.auth.context.BaseContextHandler;
import com.woldier.auth.user.annotation.LoginUser;
import com.woldier.auth.user.feign.UserQuery;
import com.woldier.auth.user.feign.UserResolveApi;
import com.woldier.auth.user.model.SysUser;
import com.woldier.auth.utils.NumberHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Token转化SysUser
 * 一个方法参数解析器
 */
@Slf4j
public class ContextArgumentResolver implements HandlerMethodArgumentResolver {

    private UserResolveApi userResolveApi;

    public ContextArgumentResolver(UserResolveApi userResolveApi) {
        this.userResolveApi = userResolveApi;
    }

    /**
     * 入参筛选 ，若要解析 需要满足相应条件
     *
     * @param mp 参数集合
     * @return 格式化后的参数
     */
    @Override
    public boolean supportsParameter(MethodParameter mp) {
        return mp.hasParameterAnnotation(LoginUser.class) && mp.getParameterType().equals(SysUser.class);
    }

    /**
     *
     * 参数的解析
     * @param methodParameter       入参集合
     * @param modelAndViewContainer model 和 view
     * @param nativeWebRequest      web相关
     * @param webDataBinderFactory  入参解析
     * @return 包装对象
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest,
                                  WebDataBinderFactory webDataBinderFactory) {
        /*获取jwt解析后的userid*/
        Long userId = BaseContextHandler.getUserId();
        /*获取jwt解析后的account*/
        String account = BaseContextHandler.getAccount();
        /*获取jwt解析后的name*/
        String name = BaseContextHandler.getName();
        /*获取jwt解析后的orgid*/
        Long orgId = BaseContextHandler.getOrgId();
        Long stationId = BaseContextHandler.getStationId();

        //以下代码为 根据 @LoginUser 注解来注入 SysUser 对象
        SysUser user = SysUser.builder()
                .id(userId)
                .account(account)
                .name(name)
                .orgId(orgId)
                .stationId(stationId)
                .build();

        try {
            /*获取方法参数的注解*/
            LoginUser loginUser = methodParameter.getParameterAnnotation(LoginUser.class);
            /*得到注解isFull*/
            boolean isFull = loginUser.isFull();
            /*判断LoginUser中有为True的吗*/
            if (isFull || loginUser.isStation() || loginUser.isOrg() || loginUser.isRoles()) {
                /*调用api*/
                R<SysUser> result = userResolveApi.getById(NumberHelper.longValueOf0(userId),
                        UserQuery.builder() //将注解中的属性转化为UserQuery对象
                                .full(isFull)
                                .org(loginUser.isOrg())
                                .station(loginUser.isStation())
                                .roles(loginUser.isRoles())
                                .build());
                /*如果api调用成功 并且 R对象中的data 不为空*/
                if (result.getIsSuccess() && result.getData() != null) {
                    return result.getData(); //返回查询到的
                }
            }
        } catch (Exception e) {
            log.warn("注入登录人信息时，发生异常. --> {}", user, e);
        }
        /*判断不成立，或者出现异常，返回最初的user*/
        return user;
    }
}
