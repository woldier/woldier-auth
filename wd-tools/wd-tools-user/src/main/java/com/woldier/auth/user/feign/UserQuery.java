package com.woldier.auth.user.feign;

import lombok.*;

/**
 * 用户查询对象
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
/**
 * 将@LoginUser注释中的属性值对象化 ，用于后续远程调用fegin （UserResolveApi）给定参数信息
 */
public class UserQuery {
    /**
     * 是否查询SysUser对象所有信息，true则通过rpc接口查询
     */
    private Boolean full;

    /**
     * 是否只查询角色信息，true则通过rpc接口查询
     */
    private Boolean roles;

    /**
     * 是否只查询组织信息，true则通过rpc接口查询
     */
    private Boolean org;

    /**
     * 是否只查询岗位信息，true则通过rpc接口查询
     */
    private Boolean station;
}
