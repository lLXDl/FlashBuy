package com.lxd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_like_record")
public class LikeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
