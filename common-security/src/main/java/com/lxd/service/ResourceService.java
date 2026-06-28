package com.lxd.service;

import com.lxd.entity.Resource;
import com.lxd.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceMapper resourceMapper;

    public List<Resource> listAll() {
        return resourceMapper.selectAllWithRoles();
    }
}
