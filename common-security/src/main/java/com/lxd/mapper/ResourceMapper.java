package com.lxd.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxd.entity.Resource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
    List<Resource> selectAllWithRoles();
}
