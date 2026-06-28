package com.lxd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ranking_snapshot")
public class RankingSnapshot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String rankingType;
    private Long targetId;
    private Integer score;
    private Integer rank;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime snapshotTime;
}
