package com.woldier.auth.user.feign;

import com.woldier.auth.base.R;
import com.woldier.auth.user.feign.fallback.UserResolveApiFallback;
import com.woldier.auth.user.model.SysUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
/**
 * 用户操作API
 */
@FeignClient(name = "${woldier.feign.authority-server:woldier-auth-server}", fallbackFactory = UserResolveApiFallback.class)
public interface UserResolveApi {
    /**
     * 根据id 查询用户详情
     */
    @PostMapping(value = "/user/anno/id/{id}")
    R<SysUser> getById(@PathVariable("id") Long id, @RequestBody UserQuery userQuery);
}