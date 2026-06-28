package com.lxd.service;

import com.lxd.annotation.Cacheable;
import com.lxd.entity.Goods;
import com.lxd.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoodsService {
    private final GoodsMapper goodsMapper;
    @Cacheable(prefix = "seckill:goods", key = "#goodsId")
    public Goods getSeckillGoods(Long goodsId) {
        return goodsMapper.selectById(goodsId);
    }
}
