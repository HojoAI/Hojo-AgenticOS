# 数据表说明（Schema 概要）

本服务使用 MyBatis-Plus，表名与实体 `repository.po` 中 `@TableName` 一致。以下为当前代码中映射的表，**非完整 DDL**；建表脚本需从数据库迁移工具或 DBA 处获取后纳入版本管理。

| 表名 | 说明 |
|------|------|
| `focus_schedule` | 日程主表 |
| `focus_schedule_detail` | 日程明细 |
| `focus_schedule_rule` | 重复规则 |
| `focus_schedule_remind` | 提醒 |
| `focus_schedule_reminder` | 提醒扩展 |
| `focus_schedule_conflict` | 冲突记录 |
| `focus_schedule_attachment` | 日程附件 |
| `focus_schedule_meeting` | 会议扩展 |
| `focus_schedule_location` | 位置 |
| `focus_schedule_flight` | 航班 |
| `focus_schedule_train` | 火车 |
| `focus_schedule_sport` | 运动 |
| `focus_schedule_movie` | 影视 |
| `focus_rule_*` | 规则侧子表（flight/train/meeting/movie/sport/location/fixed_date/attachment） |
| `focus_daily_briefing` | 日程简报 |
| `focus_map_search` | 地图搜索记录 |

字段级含义以各 PO 类字段 Javadoc 及数据库注释为准。
