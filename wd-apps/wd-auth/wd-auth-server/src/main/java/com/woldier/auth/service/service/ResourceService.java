package com.woldier.auth.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.woldier.auth.authority.dto.auth.ResourceQueryDTO;
import com.woldier.auth.authority.entity.auth.Resource;

import java.util.List;

public interface ResourceService extends IService<Resource> {
    public List<Resource> findVisibleResource(ResourceQueryDTO resourceQueryDTO);
}
