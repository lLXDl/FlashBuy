package com.lxd.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeMessage implements Serializable {
    private Long userId;
    private String targetType;
    private Long targetId;
    private Integer status; // 1-点赞，0-取消
}
