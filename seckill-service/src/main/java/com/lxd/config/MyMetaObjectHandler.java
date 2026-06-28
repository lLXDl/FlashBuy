package com.lxd.config;

import java.time.LocalDateTime;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

@Component

public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充 createTime 字段
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充 snapshotTime 字段（排行榜快照）
        this.strictInsertFill(metaObject, "snapshotTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 如需更新时间，可在这里添加
    }
}
