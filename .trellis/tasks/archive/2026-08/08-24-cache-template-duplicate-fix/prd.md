# PRD：修复 cache_info_code_template 重复缓存数据并排查其他缓存表写入竞态

## 背景

测试反馈：【安防】配置表上传后，`cache_info_code_template` 表存在同一项目（`project_id`）下同一 `custom_key` 的多条记录。

已定位根因：`CacheInfoCodeTemplateServiceImpl.replaceByCacheKeys` 采用"先按 `project_id + custom_key` 删除、再批量插入"的写入模式。该模式在**并发重建**（配置表上传、DTX 发布触发全量重建、xxl-job 初始化重建等）下存在 TOCTOU 竞态：两个请求都先查询旧键，删除窗口与插入窗口交叉时，各自插入相同 `custom_key` 的记录，且数据库无唯一索引兜底（云边同步架构禁止缓存表唯一索引，唯一性必须由业务代码维护）。

## 目标

1. 消除 `cache_info_code_template` 在并发重建下产生重复记录的问题。
2. 排查其他 `cache_*` 缓存表是否存在同类"先删后插 / 纯插入"写入模式，一并修复。
3. 提供已有重复数据的清理方案。

## 非目标

- 不加数据库唯一索引（项目规范：云边同步不支持缓存表 UNIQUE 约束）。
- 不改变 `custom_key` 的生成规则（`getTemplateTypeKey`）。
- 不重构缓存重建的整体编排结构，仅消除并发竞态。

## 约束

- 所有 `cache_*` 表禁止新增 UNIQUE 索引；唯一性由业务代码统一维护。
- 锁方案必须覆盖现有全部重建触发链路，避免遗漏互斥。
- 修复不得改变单线程下的既有行为与数据形态。

## 验收标准

1. 并发执行两次配置表上传（或上传与 DTX 发布全量重建并发）时，`cache_info_code_template` 不再出现同 `project_id + custom_key` 的重复记录。
2. `cache_object_key_rel`、`cache_rule_key_rel`、`cache_object_rel`、`cache_template_object_detail` 等同类写入模式的表在并发重建下同样不产生重复。
3. 单线程上传/重建行为与修复前一致（数据结构、键范围、时序）。
4. 提供线上重复数据清理脚本（保留每组 `project_id + custom_key` 的最新记录）。
5. 相关单元测试通过（含并发场景模拟）。
