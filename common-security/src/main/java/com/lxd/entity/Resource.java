package com.lxd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("sys_resource")
public class Resource {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String url;

    private String method;

    @TableField(exist = false)
    private List<Role> roles; // 非数据库字段，用于承载关联查询结果
}
