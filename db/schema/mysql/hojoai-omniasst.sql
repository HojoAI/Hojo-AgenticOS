-- MySQL dump 10.14  Distrib 5.5.68-MariaDB, for Linux (x86_64)
--
-- Host: mysqlc6ae18972afa.rds.ivolces.com    Database: hojoai-omniasst
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
-- Table structure for table `avatars`
--

DROP TABLE IF EXISTS `avatars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `avatars` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `avatar_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '头像业务ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '头像名称',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像描述',
  `gender` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性别',
  `avatar_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像图片URL',
  `avatar_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '形象标识符',
  `voice_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '语音标识',
  `audio_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '音频URL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_available` tinyint(1) DEFAULT '1' COMMENT '是否可用：0-不可用，1-可用',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认：0-否，1-是',
  `platform` int DEFAULT '0' COMMENT '默认是0， 0-hojo，1-豆包',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='形象表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conversations`
--

DROP TABLE IF EXISTS `conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `conversations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话唯一ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '对话标题',
  `is_read` tinyint(1) DEFAULT '1' COMMENT '是否已读：0-未读，1-已读',
  `modify_time` bigint DEFAULT NULL COMMENT '更新时间（毫秒）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `conversation_id` (`conversation_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_user_created` (`user_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=6977 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='对话会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_conversation_session`
--

DROP TABLE IF EXISTS `focus_conversation_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_conversation_session` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `conversation_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `start_intent` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '初始意图',
  `final_intent` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最终意图',
  `target_schedule_id` bigint DEFAULT NULL COMMENT '操作的日程ID',
  `turn_count` int NOT NULL DEFAULT '0' COMMENT '对话轮次数',
  `summary` text COLLATE utf8mb4_unicode_ci COMMENT 'AI生成的会话摘要',
  `start_time` datetime DEFAULT NULL COMMENT '会话开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '会话结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_user_conversation` (`user_id`,`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话Session表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_conversation_turn`
--

DROP TABLE IF EXISTS `focus_conversation_turn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_conversation_turn` (
  `id` bigint NOT NULL COMMENT '主键',
  `session_id` bigint NOT NULL COMMENT '会话Session ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `turn_index` int NOT NULL COMMENT '轮次序号',
  `role` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色: user / assistant / system',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '内容',
  `intent` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '本轮识别的意图',
  `action_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '执行的动作类型',
  `action_result` text COLLATE utf8mb4_unicode_ci COMMENT '动作结果(JSON)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_turn` (`session_id`,`turn_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话轮次表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_daily_briefing`
--

DROP TABLE IF EXISTS `focus_daily_briefing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_daily_briefing` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `briefing_date` date NOT NULL COMMENT '简报日期',
  `tips` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '提示',
  `summary_text` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '摘要文本',
  `schedule_id` bigint DEFAULT NULL COMMENT '日程id',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标题',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'icon',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_user_date` (`user_id`,`briefing_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1361793656444555265 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='每日简报表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_map_search`
--

DROP TABLE IF EXISTS `focus_map_search`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_map_search` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '地点名称',
  `location_info` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '地址信息',
  `longitude` decimal(9,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(9,6) DEFAULT NULL COMMENT '维度',
  PRIMARY KEY (`id`),
  KEY `focus_map_search_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361895088220934145 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='地图近期搜索表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_rule_attachment`
--

DROP TABLE IF EXISTS `focus_rule_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_rule_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `uid` varchar(64) NOT NULL DEFAULT '' COMMENT '前端生成id',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '文件名',
  `status` varchar(32) NOT NULL DEFAULT '' COMMENT '附件上传状态',
  `size` int DEFAULT NULL COMMENT '附件大小',
  `type` varchar(32) NOT NULL DEFAULT '' COMMENT '附件类型',
  `url` text COMMENT 'url',
  `thumb_url` text COMMENT '缩略图url',
  PRIMARY KEY (`id`),
  KEY `focus_rule_attachment_rule_id` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361524480341577730 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='规则附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_rule_fixed_date`
--

DROP TABLE IF EXISTS `focus_rule_fixed_date`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_rule_fixed_date` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `schedule_date` datetime DEFAULT NULL COMMENT '日程日期',
  PRIMARY KEY (`id`),
  KEY `focus_rule_fixed_date_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='规则固定日期表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_rule_flight`
--

DROP TABLE IF EXISTS `focus_rule_flight`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_rule_flight` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `flight_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航班号',
  `flight_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '机型简称',
  `seat_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位号',
  `boarding_gate` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登机口',
  `category` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航班属性（0：国内=国内；1：国内=国际；2：国内=地区；3：地区=国际；4：国际=国际；5：未知（=表示往返））',
  `company` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航空公司名称',
  `dep_timezone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地时区',
  `arr_timezone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地时区',
  `dep_code` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地机场三字码',
  `arr_code` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地机场三字码',
  `dep_airport` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地机场名',
  `arr_airport` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地机场名',
  `dep_city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地城市名',
  `arr_city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地城市名',
  `dep_terminal` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地航站楼',
  `arr_terminal` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地航站楼',
  `dep_plan_time` datetime DEFAULT NULL COMMENT '计划起飞时间',
  `arr_plan_time` datetime DEFAULT NULL COMMENT '计划到达时间',
  `dep_ready_time` datetime DEFAULT NULL COMMENT '预计起飞时间',
  `arr_ready_time` datetime DEFAULT NULL COMMENT '预计到达时间',
  `dep_time` datetime DEFAULT NULL COMMENT '实际起飞时间',
  `arr_time` datetime DEFAULT NULL COMMENT '实际到达时间',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航班状态',
  `stop_flag` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '经停次数（0:不经停；1:经停1次；2：经停2次；n：经停n次）',
  `leg_flag` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航段标识（0:计划航段；1:因备降而产生的航段；2:因返航而产生的航段）',
  `ontime_rate` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '历史准点率',
  `duration` int NOT NULL COMMENT '平均飞行时长',
  PRIMARY KEY (`id`),
  KEY `focus_rule_flight_rule_id` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1358685443557822465 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='规则航班表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_rule_location`
--

DROP TABLE IF EXISTS `focus_rule_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_rule_location` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '地点名称',
  `location_info` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '地址信息',
  `longitude` decimal(9,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(9,6) DEFAULT NULL COMMENT '维度',
  `province` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '省',
  `city` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '市',
  `district` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '区县',
  `township` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '街道/镇',
  PRIMARY KEY (`id`),
  KEY `focus_rule_location_rule_id` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361895084781604865 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='规则地点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_rule_meeting`
--

DROP TABLE IF EXISTS `focus_rule_meeting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_rule_meeting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `start_date` date DEFAULT NULL COMMENT '会议开始日期',
  `end_date` date DEFAULT NULL COMMENT '会议结束日期',
  `start_time` time DEFAULT NULL COMMENT '开始时间',
  `end_time` time DEFAULT NULL COMMENT '结束时间',
  `participant` varchar(512) NOT NULL DEFAULT '' COMMENT '参与人',
  `meeting_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '会议id',
  `meeting_title` varchar(128) NOT NULL DEFAULT '' COMMENT '会议标题',
  `meeting_url` varchar(512) NOT NULL DEFAULT '' COMMENT '会议url',
  `meeting_sync_time` datetime DEFAULT NULL COMMENT '会议同步时间',
  PRIMARY KEY (`id`),
  KEY `focus_rule_meeting_rule_id` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361717616640659457 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='规则会议表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_rule_movie`
--

DROP TABLE IF EXISTS `focus_rule_movie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_rule_movie` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `movie_name` varchar(128) NOT NULL DEFAULT '' COMMENT '电影名称',
  `cinema_info` varchar(256) NOT NULL DEFAULT '' COMMENT '影院信息',
  `seat_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '座位信息',
  `pic_url` varchar(512) NOT NULL DEFAULT '' COMMENT '海报地址',
  `duration` int DEFAULT NULL COMMENT '电影时长',
  PRIMARY KEY (`id`),
  KEY `focus_rule_movie_rule_id` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361895086719373313 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='规则电影表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_rule_sport`
--

DROP TABLE IF EXISTS `focus_rule_sport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_rule_sport` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `start_time` time DEFAULT NULL COMMENT '开始时间',
  `end_time` time DEFAULT NULL COMMENT '结束时间',
  `heart_rate` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '心率',
  PRIMARY KEY (`id`),
  KEY `focus_rule_sport_rule_id` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1359365299882823681 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='规则运动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_rule_train`
--

DROP TABLE IF EXISTS `focus_rule_train`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_rule_train` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `train_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '火车班次',
  `seat_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位号',
  `ticket_gate` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '检票口',
  `dep_plan_time` datetime DEFAULT NULL COMMENT '计划出发时间',
  `arr_plan_time` datetime DEFAULT NULL COMMENT '计划到达时间',
  `dep_stan` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地车站',
  `arr_stan` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地车站',
  `dep_code` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发车站三字码',
  `arr_code` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的车站三字码',
  `duration` int NOT NULL COMMENT '火车运行时间',
  PRIMARY KEY (`id`),
  KEY `focus_rule_train_rule_id` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1357274997256753153 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='规则火车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule`
--

DROP TABLE IF EXISTS `focus_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `rule_id` bigint DEFAULT NULL COMMENT '规则id',
  `sys_id` varchar(128) DEFAULT NULL COMMENT '系统日程id',
  `conversation_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '对话id',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '日程名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日程描述',
  `type` tinyint DEFAULT (-(1)) COMMENT '日程类型：-1-默认，0-任务，1-会议，2-运动',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `time_zone` varchar(64) NOT NULL DEFAULT '' COMMENT '时区',
  `is_sys_sync` tinyint NOT NULL DEFAULT '0' COMMENT '是否系统同步',
  `is_daily_briefing` tinyint DEFAULT NULL COMMENT '是否日程简报',
  `auxiliary_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '辅助信息',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待进行，1-进行中，2-已完成，3-已取消',
  `is_remind` tinyint DEFAULT '0' COMMENT '是否提醒：0-不提醒，1-提醒',
  `is_ai_suggestion` tinyint DEFAULT NULL COMMENT '是否ai建议',
  `is_conflict` tinyint DEFAULT '0' COMMENT '是否存在冲突：0-无冲突，1-有冲突',
  `conflict_count` int DEFAULT '0' COMMENT '冲突日程数量',
  `conflict_suggestion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '冲突建议',
  `tag` tinyint DEFAULT NULL COMMENT '标签（0-正常；1-Focus；2-冲突；3-建议）',
  `energy` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '精力',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
  `ai_suggestion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AI建议',
  `ai_detail_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AI类型',
  `ai_detail_title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AI详情标题',
  `is_create` tinyint DEFAULT NULL COMMENT '是否创建',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  `attachment_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'attachment json array: [{fileName,contentType,path}]',
  PRIMARY KEY (`id`),
  KEY `idx_user_conversation` (`user_id`),
  KEY `idx_user_time` (`user_id`,`start_time`,`end_time`),
  KEY `idx_sys_id` (`sys_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361895084815159297 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_attachment`
--

DROP TABLE IF EXISTS `focus_schedule_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `schedule_id` bigint DEFAULT NULL,
  `uid` varchar(64) DEFAULT '' COMMENT '前端生成id',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '文件名',
  `status` varchar(32) NOT NULL DEFAULT '' COMMENT '附件上传状态',
  `size` int DEFAULT NULL COMMENT '附件大小',
  `type` varchar(32) NOT NULL DEFAULT '' COMMENT '附件类型',
  `url` text COMMENT 'url',
  `thumb_url` text COMMENT '缩略图url',
  PRIMARY KEY (`id`),
  KEY `focus_rule_attachment_schedule_id` (`schedule_id`),
  KEY `focus_rule_attachment_rule_id` (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361524480375132162 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='规则附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_conflict`
--

DROP TABLE IF EXISTS `focus_schedule_conflict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_conflict` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `conflict_schedule_id` bigint NOT NULL COMMENT '冲突的日程ID',
  `conflict_type` tinyint NOT NULL COMMENT '冲突类型：1-完全重叠，2-部分重叠，3-前后相连',
  `overlap_minutes` int DEFAULT NULL COMMENT '重叠时长（分钟）',
  `conflict_level` tinyint DEFAULT '1' COMMENT '冲突级别：1-轻微（<=15分钟），2-中度（15-60分钟），3-严重（>60分钟）',
  `conflict_start_time` datetime DEFAULT NULL COMMENT '冲突开始时间',
  `conflict_end_time` datetime DEFAULT NULL COMMENT '冲突结束时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_conflict_schedule_id` (`conflict_schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程冲突记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_detail`
--

DROP TABLE IF EXISTS `focus_schedule_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_detail` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `high_light` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '重点建议',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删除, 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_id` (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_flight`
--

DROP TABLE IF EXISTS `focus_schedule_flight`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_flight` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `flight_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航班号',
  `flight_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '航班简称',
  `seat_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位号',
  `boarding_gate` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登机口',
  `category` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航班属性（0：国内=国内；1：国内=国际；2：国内=地区；3：地区=国际；4：国际=国际；5：未知（=表示往返））',
  `company` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航空公司名称',
  `dep_timezone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地时区',
  `arr_timezone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地时区',
  `dep_code` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地机场三字码',
  `arr_code` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地机场三字码',
  `dep_airport` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地机场名',
  `arr_airport` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地机场名',
  `dep_city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地城市名',
  `arr_city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地城市名',
  `dep_terminal` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地航站楼',
  `arr_terminal` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地航站楼',
  `dep_plan_time` datetime DEFAULT NULL COMMENT '计划起飞时间',
  `arr_plan_time` datetime DEFAULT NULL COMMENT '计划到达时间',
  `dep_ready_time` datetime DEFAULT NULL COMMENT '预计起飞时间',
  `arr_ready_time` datetime DEFAULT NULL COMMENT '预计到达时间',
  `dep_time` datetime DEFAULT NULL COMMENT '实际起飞时间',
  `arr_time` datetime DEFAULT NULL COMMENT '实际到达时间',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航班状态',
  `stop_flag` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '经停次数（0:不经停；1:经停1次；2：经停2次；n：经停n次）',
  `leg_flag` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '航段标识（0:计划航段；1:因备降而产生的航段；2:因返航而产生的航段）',
  `ontime_rate` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '历史准点率',
  `duration` int NOT NULL COMMENT '平均飞行时长',
  PRIMARY KEY (`id`),
  KEY `focus_schedule_flight_schedule_id` (`schedule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1358685443582988289 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程航班表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_location`
--

DROP TABLE IF EXISTS `focus_schedule_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_location` (
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '地点名称',
  `location_info` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '地址信息',
  `longitude` decimal(9,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(9,6) DEFAULT NULL COMMENT '维度',
  `province` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '省',
  `city` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '市',
  `district` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '区县',
  `township` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '街道/镇',
  PRIMARY KEY (`id`),
  KEY `focus_schedule_location_schedule_id` (`schedule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361895084840325121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程地点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_meeting`
--

DROP TABLE IF EXISTS `focus_schedule_meeting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_meeting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `participant` varchar(512) NOT NULL DEFAULT '' COMMENT '参与人',
  `meeting_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '会议id',
  `meeting_title` varchar(128) NOT NULL DEFAULT '' COMMENT '会议标题',
  `meeting_url` varchar(512) NOT NULL DEFAULT '' COMMENT '会议url',
  PRIMARY KEY (`id`),
  KEY `focus_schedule_meeting_schedule_id` (`schedule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361717616661630977 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程会议表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_modify_log`
--

DROP TABLE IF EXISTS `focus_schedule_modify_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_modify_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `session_id` bigint DEFAULT NULL COMMENT '触发修改的会话Session ID',
  `modify_source` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源: conversation / manual / system',
  `field_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '修改的字段名',
  `old_value` text COLLATE utf8mb4_unicode_ci COMMENT '修改前值',
  `new_value` text COLLATE utf8mb4_unicode_ci COMMENT '修改后值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_schedule_field` (`schedule_id`,`field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程修改轨迹表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_movie`
--

DROP TABLE IF EXISTS `focus_schedule_movie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_movie` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `movie_name` varchar(128) NOT NULL DEFAULT '' COMMENT '电影名称',
  `cinema_info` varchar(256) NOT NULL DEFAULT '' COMMENT '影院信息',
  `seat_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '座位信息',
  `pic_url` varchar(512) NOT NULL DEFAULT '' COMMENT '海报地址',
  `duration` int DEFAULT NULL COMMENT '电影时长',
  PRIMARY KEY (`id`),
  KEY `focus_schedule_movie_schedule_id` (`schedule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361895088204156929 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程电影表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_remind`
--

DROP TABLE IF EXISTS `focus_schedule_remind`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_remind` (
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `remind_time` datetime NOT NULL COMMENT '提醒时间',
  `remind_status` tinyint DEFAULT '0' COMMENT '提醒状态：0-未提醒，1-已提醒，2-提醒失败',
  `remind_title` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '提醒标题',
  `remind_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '提醒内容',
  `reminded_time` datetime DEFAULT NULL COMMENT '实际提醒时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_schedule_id` (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程提醒表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_reminder`
--

DROP TABLE IF EXISTS `focus_schedule_reminder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_reminder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `remind_time` datetime NOT NULL COMMENT '提醒时间',
  `remind_before_minutes` int NOT NULL COMMENT '提前提醒分钟数：如15、30、60',
  `remind_status` tinyint DEFAULT '0' COMMENT '提醒状态：0-未提醒，1-已提醒，2-提醒失败',
  `remind_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提醒方式：逗号分隔',
  `remind_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '提醒内容',
  `reminded_time` datetime DEFAULT NULL COMMENT '实际提醒时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_schedule_id` (`schedule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1357243537317564417 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程提醒表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_rule`
--

DROP TABLE IF EXISTS `focus_schedule_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `conversation_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '对话id',
  `sys_id` varchar(128) DEFAULT NULL COMMENT '系统日程id',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '日程标题',
  `type` tinyint DEFAULT (-(1)) COMMENT '日程类型：-1-默认；0-任务；1-会议；2-运动；3-电影；4-航班；5-火车',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `start_time` time DEFAULT NULL COMMENT '开始时间',
  `end_time` time DEFAULT NULL COMMENT '结束时间',
  `time_zone` varchar(64) NOT NULL DEFAULT '' COMMENT '时区',
  `is_sys_sync` tinyint NOT NULL DEFAULT '0' COMMENT '是否系统同步',
  `is_fixed_date` tinyint DEFAULT NULL COMMENT '是否固定日期',
  `is_repeat` tinyint DEFAULT NULL COMMENT '是否重复',
  `repeat_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '重复时间',
  `repeat_unit` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '重复时间单位：MM-分钟；H-小时；D-日；W-周；M-月；Y-年',
  `repeat_interval` int DEFAULT NULL COMMENT '重复时间间隔',
  `is_remind` tinyint DEFAULT '0' COMMENT '是否提醒：0-不提醒，1-提醒',
  `remind_time` int DEFAULT NULL COMMENT '重复时间',
  `remind_unit` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '重复时间单位：MM-分钟；H-小时；D-日；W-周；M-月；Y-年',
  `rule_start_time` datetime DEFAULT NULL COMMENT '规则开始时间',
  `rule_end_time` datetime DEFAULT NULL COMMENT '规则结束时间',
  `is_create_sug` tinyint DEFAULT NULL COMMENT '是否创建AI建议',
  `participant` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '参与人',
  `location_id` bigint DEFAULT NULL COMMENT '地点id',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日程备注',
  `is_create` tinyint DEFAULT NULL COMMENT '是否创建',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1361895084756439041 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_sport`
--

DROP TABLE IF EXISTS `focus_schedule_sport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_sport` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `heart_rate` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '心率',
  PRIMARY KEY (`id`),
  KEY `focus_schedule_sport_schedule_id` (`schedule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1359365299916378113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程运动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `focus_schedule_train`
--

DROP TABLE IF EXISTS `focus_schedule_train`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `focus_schedule_train` (
  `schedule_id` bigint NOT NULL COMMENT '日程ID',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `train_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '火车班次',
  `seat_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位号',
  `ticket_gate` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '检票口',
  `dep_plan_time` datetime DEFAULT NULL COMMENT '计划出发时间',
  `arr_plan_time` datetime DEFAULT NULL COMMENT '计划到达时间',
  `dep_stan` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发地车站',
  `arr_stan` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地车站',
  `dep_code` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出发车站三字码',
  `arr_code` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的车站三字码',
  `duration` int NOT NULL COMMENT '火车运行时间',
  PRIMARY KEY (`id`),
  KEY `focus_schedule_train_schedule_id` (`schedule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1357274997277724673 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日程火车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `kb_corpus`
--

DROP TABLE IF EXISTS `kb_corpus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `kb_corpus` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `snippet` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `avatar_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active',
  `corpus_type` int NOT NULL DEFAULT '0' COMMENT '0任务型 1知识型',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_conv` (`user_id`,`conversation_id`),
  KEY `idx_user_name` (`user_id`,`name`),
  KEY `idx_corpus_type` (`user_id`,`corpus_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `kb_documents`
--

DROP TABLE IF EXISTS `kb_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `kb_documents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doc_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'OSS 存储路径',
  `file_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '完整 OSS URL（供算法拉取）',
  `file_size` bigint DEFAULT NULL,
  `file_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/UPLOADED/EXTRACTING/PROCESSED/FAILED',
  `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '算法任务 ID',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_id` (`doc_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='知识库文档表（文档与OSS关系）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `kb_extract_tasks`
--

DROP TABLE IF EXISTS `kb_extract_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `kb_extract_tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `doc_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'processing' COMMENT 'processing/completed/failed',
  `progress` int DEFAULT '0',
  `message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `result_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '解析完成后的 corpuses JSON',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_doc_id` (`doc_id`),
  KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='知识提取任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `kb_pulse`
--

DROP TABLE IF EXISTS `kb_pulse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `kb_pulse` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `corpus_id` bigint unsigned NOT NULL,
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_corpus` (`corpus_id`,`deleted`,`sort_order`),
  KEY `idx_user` (`user_id`),
  CONSTRAINT `fk_pulse_corpus` FOREIGN KEY (`corpus_id`) REFERENCES `kb_corpus` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识脉络';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `meeting_import_tasks`
--

DROP TABLE IF EXISTS `meeting_import_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `meeting_import_tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `meeting_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议唯一ID，关联 meetings.meeting_id',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `original_filename` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户选择的原始文件名，用作默认标题展示',
  `file_ext` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件扩展名（小写，如 mp3/wav/m4a）',
  `file_size_bytes` bigint DEFAULT NULL COMMENT '文件大小（字节），服务端校验与展示用',
  `content_type` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'MIME 类型（如 audio/mpeg），可选',
  `oss_object_key` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上传完成后的 OSS 对象键（或最终可访问路径标识）',
  `source` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'FILE_IMPORT' COMMENT '来源：FILE_IMPORT=文件导入，可与实时录音扩展区分',
  `recording_category` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING_CLASSIFY' COMMENT '录音类型：PENDING_CLASSIFY=待分类，MEETING=会议，MUSIC=音乐，AMBIENT=环境/无有效语音',
  `pipeline_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING_UPLOAD' COMMENT '主流程状态：PENDING_UPLOAD=待上传完成，TRANSCRIBING=转写中，ANALYZING=智能解读中（纪要+解读，不含知识），COMPLETED=主流程已完成，FAILED=失败',
  `knowledge_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '知识模块：PENDING=未开始，PROCESSING=处理中，DONE=完成，SKIPPED=跳过（非会议等），FAILED=失败',
  `oss_multipart_upload_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'OSS 分片上传 uploadId，用于断点续传',
  `oss_uploaded_parts_json` json DEFAULT NULL COMMENT '已上传分片信息（如 [{partNumber, etag}]），用于续传合并',
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败时的业务错误码，便于端上展示与统计',
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因简述',
  `estimated_remaining_minutes` int DEFAULT NULL COMMENT '预估剩余分钟数（转写阶段等，可空）',
  `is_new` tinyint NOT NULL DEFAULT '1' COMMENT '是否在列表展示「新」标记：1=未读，0=已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_id` (`meeting_id`),
  KEY `idx_user_created` (`user_id`,`created_at`),
  KEY `idx_user_knowledge` (`user_id`,`knowledge_status`),
  KEY `idx_user_pipeline` (`user_id`,`pipeline_status`)
) ENGINE=InnoDB AUTO_INCREMENT=256 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议录音导入任务与异步处理状态（上传/转写/解读/知识子流程）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `meeting_segment_speaker`
--

DROP TABLE IF EXISTS `meeting_segment_speaker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `meeting_segment_speaker` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `meeting_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议ID，如 M136147...',
  `segment_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '段落/录音段ID，如 136147...',
  `speaker_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '候选发言人ID（建议 local-{uuid}）',
  `display_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发言人展示名',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_segment_speaker` (`meeting_id`,`segment_id`,`speaker_id`),
  KEY `idx_meeting_segment` (`meeting_id`,`segment_id`),
  KEY `idx_meeting_segment_name` (`meeting_id`,`segment_id`,`display_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议段落历史新增发言人表（不入声纹库）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `meeting_segments`
--

DROP TABLE IF EXISTS `meeting_segments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `meeting_segments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `meeting_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属会议ID',
  `segment_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '段落唯一ID',
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文本内容',
  `speaker_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '说话人ID',
  `speaker_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '说话人姓名',
  `speaker_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '说话人头像URL',
  `start_time` bigint NOT NULL COMMENT '段落开始时间（相对时间，毫秒）',
  `end_time` bigint NOT NULL COMMENT '段落结束时间（相对时间，毫秒）',
  `duration` bigint NOT NULL COMMENT '段落时长（毫秒）',
  `timestamp` bigint NOT NULL COMMENT '消息时间戳',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mark_json` text COLLATE utf8mb4_unicode_ci COMMENT '段落打标 JSON，如 offsetMs、markedAt',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_segment` (`meeting_id`,`segment_id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_segment_id` (`segment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=27239 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='会议段落存储表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `meetings`
--

DROP TABLE IF EXISTS `meetings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `meetings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `meeting_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会议唯一ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会议标题',
  `meeting_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会议类型',
  `start_time` bigint DEFAULT NULL COMMENT '会议开始时间戳（毫秒）',
  `end_time` bigint DEFAULT NULL COMMENT '会议结束时间戳（毫秒）',
  `total_duration` bigint DEFAULT NULL COMMENT '总时长（毫秒）',
  `summary_status` tinyint DEFAULT '0' COMMENT 'AI摘要状态（0=未完成，1=已完成）',
  `oss_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '完整音频OSS路径',
  `segments_json` json DEFAULT NULL COMMENT '转写分段列表',
  `attendees` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '参会人员列表（JSON数组字符串）',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '会议总结',
  `bex_interpretation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'Bex解读',
  `chapter_skim` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'AI生成：章节速览（JSON数组字符串）',
  `core_topics` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'AI生成：核心议题（JSON数组字符串）',
  `related_materials` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'AI生成：相关资料（JSON数组字符串）',
  `knowledge` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'AI生成：知识点列表（JSON数组字符串）',
  `knowledge_count` int DEFAULT '0' COMMENT '知识点个数',
  `attendee_bex_analysis` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'AI生成：参会者行为分析（JSON数组字符串）',
  `bex_advice` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'AI生成：BEX建议（JSON对象字符串，包含deductions和credits）',
  `todo` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'AI生成：待办事项（JSON数组字符串）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `meeting_id` (`meeting_id`),
  KEY `idx_summary_status` (`summary_status`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_user_created` (`user_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1467 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='会议记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息唯一ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属对话ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息角色(user/assistant/system)',
  `content_json` json DEFAULT NULL COMMENT '结构化内容（JSON格式，支持多模态）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `timestamp` bigint DEFAULT NULL COMMENT '时间戳',
  `response_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回答的消息id',
  `avatar_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '形象ID',
  `is_read` tinyint(1) DEFAULT '1' COMMENT '是否已读：0-未读，1-已读',
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Chat' COMMENT '消息类型',
  PRIMARY KEY (`id`),
  UNIQUE KEY `message_id` (`message_id`),
  KEY `idx_message_id` (`message_id`),
  KEY `idx_role` (`role`),
  KEY `idx_conv_time` (`conversation_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=29295 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pa_message`
--

DROP TABLE IF EXISTS `pa_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pa_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `message_id` varchar(64) NOT NULL COMMENT '业务主键',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `type` varchar(32) NOT NULL COMMENT 'meeting/translation/system',
  `title` varchar(256) NOT NULL DEFAULT '' COMMENT '标题',
  `body` varchar(1024) DEFAULT NULL COMMENT '正文摘要',
  `related_id` varchar(64) DEFAULT NULL COMMENT '关联业务ID',
  `related_title` varchar(256) DEFAULT NULL COMMENT '关联业务标题',
  `landing_page` varchar(512) DEFAULT NULL COMMENT '跳转路径',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` datetime DEFAULT NULL COMMENT '已读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_user_created` (`user_id`,`created_at` DESC),
  KEY `idx_user_read` (`user_id`,`read_at`)
) ENGINE=InnoDB AUTO_INCREMENT=375 DEFAULT CHARSET=utf8mb3 COMMENT='消息中心-消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pa_message_task`
--

DROP TABLE IF EXISTS `pa_message_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pa_message_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` varchar(64) NOT NULL COMMENT '业务主键',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `type` varchar(32) NOT NULL COMMENT 'meeting/translation',
  `title` varchar(256) NOT NULL DEFAULT '' COMMENT '标题',
  `status` varchar(16) NOT NULL DEFAULT 'running' COMMENT 'running/completed',
  `progress_text` varchar(256) DEFAULT NULL COMMENT '进度文案',
  `estimated_seconds` int DEFAULT NULL COMMENT '预计剩余秒数',
  `related_id` varchar(64) DEFAULT NULL COMMENT '关联业务ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `finished_at` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_user_status_created` (`user_id`,`status`,`created_at` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=386 DEFAULT CHARSET=utf8mb3 COMMENT='消息中心-进程任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pa_recommendation`
--

DROP TABLE IF EXISTS `pa_recommendation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pa_recommendation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `recommendation_id` varchar(64) NOT NULL COMMENT '业务主键',
  `user_id` varchar(64) NOT NULL,
  `type` varchar(32) NOT NULL COMMENT 'schedule/meeting/knowledge/explore',
  `title` varchar(256) NOT NULL DEFAULT '',
  `body` varchar(1024) DEFAULT NULL COMMENT '建议话术',
  `related_id` varchar(64) DEFAULT NULL COMMENT '关联业务ID',
  `related_type` varchar(32) DEFAULT NULL COMMENT '关联业务类型',
  `landing_page` varchar(512) DEFAULT NULL COMMENT '跳转路径',
  `extra` text COMMENT '类型特有业务数据(JSON)',
  `attachments` text COMMENT '文件附件列表(JSON数组)',
  `status` varchar(16) NOT NULL DEFAULT 'active' COMMENT 'active/ignored/expired',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间，NULL表示不过期',
  `ignored_at` datetime DEFAULT NULL COMMENT '忽略时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recommendation_id` (`recommendation_id`),
  KEY `idx_user_status_expires` (`user_id`,`status`,`expires_at`),
  KEY `idx_user_type_related` (`user_id`,`type`,`related_id`),
  KEY `idx_user_type_ignored` (`user_id`,`type`,`status`,`ignored_at`)
) ENGINE=InnoDB AUTO_INCREMENT=631 DEFAULT CHARSET=utf8mb3 COMMENT='为你推荐 - 推荐内容表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pa_recommendation_suppress`
--

DROP TABLE IF EXISTS `pa_recommendation_suppress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pa_recommendation_suppress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL,
  `type` varchar(32) NOT NULL COMMENT 'schedule/meeting/knowledge/explore',
  `ignore_count` int NOT NULL DEFAULT '0' COMMENT '近7天忽略累计次数',
  `suppressed_until` datetime DEFAULT NULL COMMENT '该类型在此时间前不推荐',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_type` (`user_id`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COMMENT='为你推荐 - 负反馈抑制表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rtc_conversation`
--

DROP TABLE IF EXISTS `rtc_conversation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rtc_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `device_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备ID',
  `rtc_room_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'RTC房间ID',
  `message_count` int DEFAULT '0' COMMENT '消息总数（冗余字段，便于统计）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_room_index` (`user_id`,`rtc_room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='rtc对话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rtc_conversation_message`
--

DROP TABLE IF EXISTS `rtc_conversation_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rtc_conversation_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rtc_room_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'RTC房间ID',
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息类型：user（ASR识别结果），ai（TTS文本）',
  `message_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容（文本）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_user_room_index` (`user_id`,`rtc_room_id`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='rtc对话消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `translation_assets`
--

DROP TABLE IF EXISTS `translation_assets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `translation_assets` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `translation_asset_id` varchar(64) NOT NULL COMMENT '业务主键（UUID）',
  `translation_conversation_id` varchar(64) NOT NULL COMMENT '所属会话 ID',
  `asset_type` varchar(16) NOT NULL COMMENT '资源类型：IMAGE / AUDIO',
  `origin_url` varchar(1024) NOT NULL COMMENT '原始资源 OSS URL',
  `translated_url` varchar(1024) DEFAULT NULL COMMENT 'OCR 合成图 URL（IMAGE 类型用）',
  `ocr_text` text COMMENT 'OCR 识别原文',
  `translated_text` text COMMENT '翻译结果文本',
  `capture_ts` bigint NOT NULL COMMENT '拍照或音频分段开始时间戳（ms）',
  `duration_ms` bigint DEFAULT NULL COMMENT '音频分段时长（ms，AUDIO 类型用）',
  `status` varchar(16) NOT NULL DEFAULT 'PROCESSING' COMMENT '处理状态：PROCESSING / SUCCESS / FAILED',
  `ext` json DEFAULT NULL COMMENT '扩展信息',
  `created_ts` bigint NOT NULL COMMENT '创建时间戳（ms）',
  `updated_ts` bigint NOT NULL COMMENT '更新时间戳（ms）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_id` (`translation_asset_id`),
  KEY `idx_conversation` (`translation_conversation_id`),
  KEY `idx_conversation_capture` (`translation_conversation_id`,`capture_ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='翻译资源表（OCR图片 / 分段音频）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `translation_conversations`
--

DROP TABLE IF EXISTS `translation_conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `translation_conversations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `translation_conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '翻译会话唯一ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `source_language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源语言',
  `target_language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标语言',
  `start_time` bigint DEFAULT NULL COMMENT '会话开始时间戳（毫秒）',
  `end_time` bigint DEFAULT NULL COMMENT '会话结束时间戳（毫秒）',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '会话标题（短标题，便于列表展示）',
  `summary` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '会话摘要，较长的自动生成摘要，区别于标题',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `duration` bigint DEFAULT NULL COMMENT '会话总时长（毫秒），会话结束时计算并存储',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否正在进行：1=进行中，0=已结束',
  `conversation_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'FACE_TO_FACE' COMMENT 'FACE_TO_FACE/SIMULTANEOUS',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING/COMPLETED/FAILED',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '软删除 0-正常 1-已删除',
  `summary_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SKIPPED' COMMENT 'SKIPPED/GENERATING/COMPLETED/FAILED',
  `summary_content` text COLLATE utf8mb4_unicode_ci COMMENT 'AI总结全文',
  `summary_ts` bigint DEFAULT NULL COMMENT 'AI总结生成时间戳ms',
  `audio_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话音频OSS URL（同声传译合并后）',
  `audio_duration_ms` bigint DEFAULT NULL COMMENT '录音时长ms',
  `scene_ext` json DEFAULT NULL COMMENT '会话配置扩展信息快照',
  PRIMARY KEY (`id`),
  UNIQUE KEY `translation_conversation_id` (`translation_conversation_id`),
  KEY `idx_conversation_id` (`translation_conversation_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_user_type_time` (`user_id`,`conversation_type`,`start_time`),
  KEY `idx_status_del` (`status`,`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=1886 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='翻译会话表（同声传译场景）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `translation_messages`
--

DROP TABLE IF EXISTS `translation_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `translation_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `translation_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '翻译消息唯一ID',
  `translation_conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属翻译会话ID',
  `source_language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源语种（配置项）',
  `target_language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标语种（配置项）',
  `original_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原文',
  `translated_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '译文',
  `detected_language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '检测到的语言',
  `timestamp` bigint NOT NULL COMMENT '时间戳',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `message_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT_SEGMENT' COMMENT 'TEXT_SEGMENT/OCR_IMAGE/SYSTEM',
  `sequence_no` bigint DEFAULT NULL COMMENT '同会话顺序号，用于客户端排序',
  `asset_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联资源ID（OCR_IMAGE 场景关联 translation_assets）',
  `payload_ext` json DEFAULT NULL COMMENT '扩展字段（同传场景存字级时间戳 words 等）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conv_timestamp` (`translation_conversation_id`,`timestamp`),
  UNIQUE KEY `translation_message_id` (`translation_message_id`),
  KEY `idx_message_id` (`translation_message_id`),
  KEY `idx_conv_created` (`translation_conversation_id`,`created_at`),
  KEY `idx_conv_seq` (`translation_conversation_id`,`sequence_no`),
  KEY `idx_conv_ts` (`translation_conversation_id`,`timestamp`)
) ENGINE=InnoDB AUTO_INCREMENT=11214 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='翻译内容表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_avatars`
--

DROP TABLE IF EXISTS `user_avatars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_avatars` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `avatar_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '头像ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=119 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户头像关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'hojoai-omniasst'
--

--
-- Dumping routines for database 'hojoai-omniasst'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-16 11:57:54