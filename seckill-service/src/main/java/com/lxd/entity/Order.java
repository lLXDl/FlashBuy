package com.lxd.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {
    // 1. 主键策略：改为 ASSIGN_ID，使用雪花算法生成全局唯一ID
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long goodsId;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}