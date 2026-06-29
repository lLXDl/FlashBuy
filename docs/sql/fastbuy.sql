/*
 Navicat MySQL Data Transfer

 Source Server         : LXD
 Source Server Type    : MySQL
 Source Server Version : 80023
 Source Host           : localhost:3306
 Source Schema         : fastbuy

 Target Server Type    : MySQL
 Target Server Version : 80023
 File Encoding         : 65001

 Date: 29/06/2026 09:26:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ranking_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `ranking_snapshot`;
CREATE TABLE `ranking_snapshot`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标类型：goods/user',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `snapshot_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ranking_snapshot`(`target_type`, `target_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排行榜快照表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ranking_snapshot
-- ----------------------------
INSERT INTO `ranking_snapshot` VALUES (1, 'goods', 1, 2, '2026-06-27 09:30:00');
INSERT INTO `ranking_snapshot` VALUES (2, 'goods', 2, 1, '2026-06-27 09:30:00');
INSERT INTO `ranking_snapshot` VALUES (3, 'user', 2, 5, '2026-06-27 09:30:00');
INSERT INTO `ranking_snapshot` VALUES (4, 'user', 3, 3, '2026-06-27 09:30:00');

-- ----------------------------
-- Table structure for sys_resource
-- ----------------------------
DROP TABLE IF EXISTS `sys_resource`;
CREATE TABLE `sys_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源路径，如 /api/user/**',
  `method` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'ALL' COMMENT '请求方法: GET, POST, ALL等',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资源描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_resource
-- ----------------------------
INSERT INTO `sys_resource` VALUES (1, '/api/admin/**', 'ALL', '管理员接口');
INSERT INTO `sys_resource` VALUES (2, '/api/user/**', 'ALL', '用户接口');
INSERT INTO `sys_resource` VALUES (3, '/api/goods/**', 'GET', '商品查询接口');
INSERT INTO `sys_resource` VALUES (4, '/api/order/**', 'ALL', '订单接口');

-- ----------------------------
-- Table structure for sys_resource_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_resource_role`;
CREATE TABLE `sys_resource_role`  (
  `resource_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`resource_id`, `role_id`) USING BTREE,
  INDEX `role_id`(`role_id`) USING BTREE,
  CONSTRAINT `sys_resource_role_ibfk_1` FOREIGN KEY (`resource_id`) REFERENCES `sys_resource` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `sys_resource_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_resource_role
-- ----------------------------
INSERT INTO `sys_resource_role` VALUES (1, 1);
INSERT INTO `sys_resource_role` VALUES (2, 1);
INSERT INTO `sys_resource_role` VALUES (3, 1);
INSERT INTO `sys_resource_role` VALUES (4, 1);
INSERT INTO `sys_resource_role` VALUES (2, 2);
INSERT INTO `sys_resource_role` VALUES (3, 2);
INSERT INTO `sys_resource_role` VALUES (4, 2);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色编码，如 ROLE_ADMIN',
  `role_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ROLE_ADMIN', '管理员');
INSERT INTO `sys_role` VALUES (2, 'ROLE_USER', '普通用户');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '加密密码',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, '2026-06-27 09:00:00');
INSERT INTO `sys_user` VALUES (2, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, '2026-06-27 09:05:00');
INSERT INTO `sys_user` VALUES (3, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, '2026-06-27 09:10:00');
INSERT INTO `sys_user` VALUES (6, 'user3', '$2a$10$RDkXJZ/zQIXRfYwN16oq0eMCZI5BXoxCRAKC4nSJTlCWTg4M5HVDC', 1, NULL);
INSERT INTO `sys_user` VALUES (7, 'user4', '$2a$10$4hIYcpAcDPJQdMvGCr2ifuVGyTkgznSPfHYGOtPPHbsfQQRVryts6', 1, NULL);
INSERT INTO `sys_user` VALUES (8, 'user5', '$2a$10$ampl6iS7qXTKwAOPa3hfP.RTiHrSdLukZpdUx1zRPTcDUN0AKdjyO', 1, NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE,
  INDEX `fk_user_role_role`(`role_id`) USING BTREE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2, 2);
INSERT INTO `sys_user_role` VALUES (3, 3, 2);
INSERT INTO `sys_user_role` VALUES (4, 6, 2);
INSERT INTO `sys_user_role` VALUES (5, 7, 2);
INSERT INTO `sys_user_role` VALUES (6, 8, 2);

-- ----------------------------
-- Table structure for t_goods
-- ----------------------------
DROP TABLE IF EXISTS `t_goods`;
CREATE TABLE `t_goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stock` int NOT NULL COMMENT '剩余库存',
  `version` int NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_goods
-- ----------------------------
INSERT INTO `t_goods` VALUES (1, '无线蓝牙耳机', 100, 0);
INSERT INTO `t_goods` VALUES (2, '机械键盘', 50, 0);
INSERT INTO `t_goods` VALUES (3, 'USB-C 充电线', 199, 0);

-- ----------------------------
-- Table structure for t_like_record
-- ----------------------------
DROP TABLE IF EXISTS `t_like_record`;
CREATE TABLE `t_like_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标类型：goods/comment',
  `target_id` bigint NOT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '1-点赞，0-取消',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id`, `target_type`, `target_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_like_record
-- ----------------------------
INSERT INTO `t_like_record` VALUES (1, 2, 'goods', 1, 1, '2026-06-27 09:16:00');
INSERT INTO `t_like_record` VALUES (2, 3, 'goods', 1, 1, '2026-06-27 09:21:00');
INSERT INTO `t_like_record` VALUES (3, 2, 'goods', 2, 1, '2026-06-27 09:26:00');
INSERT INTO `t_like_record` VALUES (4, 3, 'goods', 2, 1, '2026-06-27 09:30:00');
INSERT INTO `t_like_record` VALUES (5, 2, 'comment', 101, 1, '2026-06-27 09:28:00');
INSERT INTO `t_like_record` VALUES (17, 8, 'goods', 2, 1, '2026-06-28 20:50:47');

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `status` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_goods`(`user_id`, `goods_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2071066430161289218 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_order
-- ----------------------------
INSERT INTO `t_order` VALUES (1, 2, 1, 0, '2026-06-27 09:15:00');
INSERT INTO `t_order` VALUES (2, 3, 1, 1, '2026-06-27 09:20:00');
INSERT INTO `t_order` VALUES (3, 2, 2, 0, '2026-06-27 09:25:00');
INSERT INTO `t_order` VALUES (2070865242375602178, 6, 1, 2, '2026-06-27 21:42:03');
INSERT INTO `t_order` VALUES (2071056480475357185, 6, 3, 2, '2026-06-28 10:21:58');
INSERT INTO `t_order` VALUES (2071060321824481282, 6, 2, 2, '2026-06-28 10:37:14');
INSERT INTO `t_order` VALUES (2071063884558614530, 7, 1, 2, '2026-06-28 10:51:23');
INSERT INTO `t_order` VALUES (2071064087701340161, 7, 2, 2, '2026-06-28 10:52:12');
INSERT INTO `t_order` VALUES (2071064647154343937, 7, 3, 2, '2026-06-28 10:54:25');
INSERT INTO `t_order` VALUES (2071065248017752065, 8, 1, 2, '2026-06-28 10:56:48');
INSERT INTO `t_order` VALUES (2071065547834933249, 8, 2, 2, '2026-06-28 10:58:00');
INSERT INTO `t_order` VALUES (2071066430161289218, 8, 3, 1, '2026-06-28 11:01:30');

-- ----------------------------
-- Table structure for t_ranking_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `t_ranking_snapshot`;
CREATE TABLE `t_ranking_snapshot`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ranking_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'daily/weekly/monthly',
  `target_id` bigint NOT NULL,
  `score` int NOT NULL COMMENT '分数',
  `rank` int NOT NULL COMMENT '排名',
  `snapshot_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ranking_target`(`ranking_type`, `target_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_ranking_snapshot
-- ----------------------------
INSERT INTO `t_ranking_snapshot` VALUES (1, 'daily', 1, 98, 1, '2026-06-27 00:00:00');
INSERT INTO `t_ranking_snapshot` VALUES (2, 'daily', 2, 75, 2, '2026-06-27 00:00:00');
INSERT INTO `t_ranking_snapshot` VALUES (3, 'weekly', 1, 650, 1, '2026-06-22 00:00:00');
INSERT INTO `t_ranking_snapshot` VALUES (4, 'weekly', 2, 420, 2, '2026-06-22 00:00:00');
INSERT INTO `t_ranking_snapshot` VALUES (5, 'monthly', 1, 2800, 1, '2026-06-01 00:00:00');
INSERT INTO `t_ranking_snapshot` VALUES (6, 'monthly', 2, 1950, 2, '2026-06-01 00:00:00');

SET FOREIGN_KEY_CHECKS = 1;
