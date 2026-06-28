package com.lxd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_goods")
public class Goods {

    // 1. 主键策略：改为 ASSIGN_ID，使用雪花算法生成全局唯一ID
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String goodsName;

    private Integer stock;
}