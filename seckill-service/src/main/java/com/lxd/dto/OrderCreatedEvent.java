package com.lxd.dto;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 订单创建事件 - 用于事务提交后触发缓存删除
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private Long goodsId;
}
