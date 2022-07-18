package com.woldier.auth.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.woldier.auth.authority.dto.auth.ResourceQueryDTO;
import com.woldier.auth.authority.entity.auth.Resource;
import com.woldier.auth.authority.entity.auth.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper extends BaseMapper<User> {

}
