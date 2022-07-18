package com.woldier.auth.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.woldier.auth.authority.dto.auth.ResourceQueryDTO;
import com.woldier.auth.authority.entity.auth.Resource;
import com.woldier.auth.service.mapper.ResourceMapper;
import com.woldier.auth.service.service.ResourceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService
{


    /**
     *
     * @param resourceQueryDTO
     * @return
     */
    @Override
    public List<Resource> findVisibleResource(ResourceQueryDTO resourceQueryDTO) {
        return baseMapper.findVisibleResource(resourceQueryDTO);
    }
}
