package com.lxd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResp {
    private String token;
    private Long userId;
}
