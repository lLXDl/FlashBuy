package com.lxd.controller;

import com.lxd.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    /**
     * 查询用户订单列表
     */
    @GetMapping("/list")
    public List<Map<String, Object>> getUserOrders(@RequestParam Long userId) {
        return orderService.getUserOrders(userId);
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay/{orderId}")
    public Map<String, Object> payOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        return orderService.payOrder(orderId, userId);
    }
}