package com.lxd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxd.entity.LikeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {
    @Insert("INSERT INTO t_like_record(user_id, target_type, target_id, status) " +
            "VALUES(#{userId}, #{targetType}, #{targetId}, #{status}) " +
            "ON DUPLICATE KEY UPDATE status = #{status}")
    int upsert(LikeRecord record);
}