package com.lxd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxd.entity.RankingSnapshot;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RankingSnapshotMapper extends BaseMapper<RankingSnapshot> {
    @Insert("INSERT INTO t_ranking_snapshot (ranking_type, target_id, score, `rank`, snapshot_time) " +
            "VALUES (#{rankingType}, #{targetId}, #{score}, #{rank}, NOW()) " +
            "ON DUPLICATE KEY UPDATE score = #{score}, `rank` = #{rank}, snapshot_time = NOW()")
    int upsert(RankingSnapshot snapshot);
}
