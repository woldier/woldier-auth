package com.woldier.auth.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.woldier.auth.authority.entity.auth.User;
import com.woldier.auth.service.mapper.UserMapper;
import com.woldier.auth.service.service.UserService;
import org.springframework.stereotype.Service;

@Service

public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
