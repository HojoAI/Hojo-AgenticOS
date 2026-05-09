-- MySQL dump 10.14  Distrib 5.5.68-MariaDB, for Linux (x86_64)
--
-- Host: mysqlc6ae18972afa.rds.ivolces.com    Database: hojoai-central-account
-- ------------------------------------------------------
-- Server version       8.0.43-34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `oversea_auth_identity`
--

DROP TABLE IF EXISTS `oversea_auth_identity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oversea_auth_identity` (
  `id` bigint NOT NULL COMMENT '雪花算法 ID',
  `user_id` bigint NOT NULL COMMENT '关联 oversea_user_info.id',
  `provider` varchar(20) NOT NULL COMMENT 'google / apple / email',
  `provider_user_id` varchar(255) NOT NULL COMMENT '第三方用户标识（Google sub / Apple sub / email 地址）',
  `email` varchar(255) DEFAULT NULL COMMENT '该身份关联的邮箱',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_uid` (`provider`,`provider_user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='海外用户认证身份';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oversea_user_info`
--

DROP TABLE IF EXISTS `oversea_user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oversea_user_info` (
  `id` bigint NOT NULL COMMENT '雪花算法 ID',
  `email` varchar(255) DEFAULT NULL COMMENT '主邮箱',
  `nickname` varchar(100) DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '用户头像 URL',
  `status` varchar(16) NOT NULL DEFAULT 'active' COMMENT 'active / disabled / deleted',
  `token_version` int NOT NULL DEFAULT '0' COMMENT 'Token 版本号，递增后使该用户所有已签发 Token 失效',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  `auto_save_kb` tinyint(1) DEFAULT '0' COMMENT '是否自动保存到知识库',
  `auto_save_cloud` tinyint(1) DEFAULT '0' COMMENT '是否自动保存到云盘',
  `birthday` varchar(50) DEFAULT NULL COMMENT '生日',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `recommend_for_user` tinyint(1) DEFAULT '1' COMMENT '为你推荐开关',
  `use_memory` tinyint(1) DEFAULT '1' COMMENT '记忆开关',
  `preferred_model` varchar(64) DEFAULT NULL COMMENT '首选模型，对应 model-list 的 model',
  `notify_push` tinyint(1) NOT NULL DEFAULT '1',
  `notify_calendar` tinyint(1) NOT NULL DEFAULT '1',
  `notify_product` tinyint(1) NOT NULL DEFAULT '1',
  `notify_agent` tinyint(1) NOT NULL DEFAULT '1',
  `preferred_language` varchar(64) DEFAULT NULL,
  `subscription_type` varchar(64) DEFAULT NULL COMMENT '订阅方案展示文案',
  `used_space` bigint DEFAULT NULL COMMENT '已用空间（字节）',
  `total_space` bigint DEFAULT NULL COMMENT '总空间配额（字节）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='海外用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_info` (
  `incr_id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `user_mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '用户手机',
  `user_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户密码',
  `user_nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户昵称',
  `user_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户简介',
  `user_head_image` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户头像',
  `disabled_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '禁用状态',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `auto_save_kb` tinyint(1) DEFAULT '0' COMMENT '是否自动保存到知识库',
  `auto_save_cloud` tinyint(1) DEFAULT '0' COMMENT '是否自动保存到云盘',
  `birthday` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生日',
  `gender` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '性别',
  `recommend_for_user` tinyint(1) DEFAULT '1' COMMENT '为你推荐开关',
  `use_memory` tinyint(1) DEFAULT '1' COMMENT '记忆开关',
  `preferred_model` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '首选模型，对应 model-list 的 model',
  `notify_push` tinyint(1) NOT NULL DEFAULT '1' COMMENT '推送通知总开关 1开0关',
  `notify_calendar` tinyint(1) NOT NULL DEFAULT '1' COMMENT '日历事件通知',
  `notify_product` tinyint(1) NOT NULL DEFAULT '1' COMMENT '产品动态通知',
  `notify_agent` tinyint(1) NOT NULL DEFAULT '1' COMMENT '智能体工作消息通知',
  `preferred_language` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '首选界面语言',
  `subscription_type` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订阅方案展示文案',
  `used_space` bigint DEFAULT NULL COMMENT '已用空间（字节）',
  `total_space` bigint DEFAULT NULL COMMENT '总空间配额（字节）',
  PRIMARY KEY (`incr_id`),
  UNIQUE KEY `unique_user_id` (`user_id`),
  UNIQUE KEY `unique_user_mobile` (`user_mobile`)
) ENGINE=InnoDB AUTO_INCREMENT=162 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_token_info`
--

DROP TABLE IF EXISTS `user_token_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_token_info` (
  `incr_id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `business_type` tinyint(1) NOT NULL COMMENT '业务类型',
  `platform_type` tinyint(1) NOT NULL COMMENT '平台类型',
  `user_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '令牌代码',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`incr_id`),
  UNIQUE KEY `unique_user_id_business_type_platform_type` (`user_id`,`business_type`,`platform_type`)
) ENGINE=InnoDB AUTO_INCREMENT=377 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户令牌信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'hojoai-central-account'
--

--
-- Dumping routines for database 'hojoai-central-account'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-17 19:33:51