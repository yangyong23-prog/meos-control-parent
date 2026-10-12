# 缓存对象模型重构设计

## 1. Final Architecture

云端负责构建七张新版缓存表并提交 READY；边缘端通过既有云边同步接收表数据和
`rc_basics_config` 状态。业务请求只读取当前部署环境的 Redis READY，不同步查询数据库。

当前迁移范围固定为 `AIR_TERMINAL`、`LIGHTING`：

- READY：只读新版缓存，缺失即按实际命中返回。
- 未 READY：实时查询 RWD 或规则服务，不读旧版持久化运行结果缓存。
- 其他运行监测系统：继续使用旧版读写链路。

`cache_rwd_object`、`cache_iot_rule` 等旧表仍保留，但不再由末端、照明构建或消费。

## 2. Data Ownership

| 表 | 业务所有权 |
| --- | --- |
| `cache_object` | 跨 Key 共享的对象公共列和对象扩展 JSON |
| `cache_object_key_rel` | 新版业务 Key 到对象 ID 的完整范围 |
| `cache_object_rel` | 按图谱、关系类型和主体维护的对象关系 |
| `cache_object_model_view` | 与对象 ID 一一对应的模型视图公共列和视图 JSON |
| `cache_template_object_detail` | 模板 Key 下自包含的信息点对象明细 |
| `cache_rule` | 按业务键去重的规则主记录 |
| `cache_rule_key_rel` | 单 Key 的规则写入归属与孤儿清理依据 |

对象和模型视图使用两份独立的 classCode 字段 Map。公共列是可信数据源；JSON 只保存
配置允许且 RWD 实际返回的字段，无扩展字段时保存 `{}`。模型视图不按 objType 隐式注入
`bimId`、`outline` 等字段，这些字段必须在对应 classCode 的视图 Map 中明确配置。

`monitoring_surveillance_status` 是可空单值字符串列。查询参数可提供多个候选 code，
但恢复到 DTO 后仍是标量，`isControlled()` 只按字符串 `2`、`3` 判断。

## 3. Write Boundaries

公共对象写入接口为：

```java
boolean replaceModelCacheKey(
        String modelCacheKey,
        Collection<? extends DtpRwdObjectDTO> sourceObjects);

boolean replaceModelCacheKey(
        String modelCacheKey,
        Collection<? extends DtpRwdObjectDTO> sourceObjects,
        Collection<CacheWriteAction> writeActions);

void replaceObjectRelations(
        DtpRwdRelationTypeEnum relationType,
        Collection<? extends DtpRwdObjectDTO> relationObjects);
```

调用方先完成 RWD 查询、完整字段准备、关系方向归一化和冲突校验。对象入口在事务前统一投影
`CacheObject` 与 `CacheObjectModelView`，短事务内提交对象、视图、Key 关联和已准备的
明细/规则动作。

关系入口是独立短事务。每个入参对象的 `id` 是主体，`relObjIds` 是完整目标集合；
目标为空表示清理该主体范围。空对象集合表示本次不维护该关系类型。同一调用内主体必须唯一，
正反向查询得到同类型关系时由调用方先按主体合并。

写服务不接收关系查询参数，不持有 RWD 客户端，也不接受
`CacheObjectRelReplaceScope` 等资源层参数。

## 4. Rebuild Semantics

每个新版 Key 独立执行：

1. 事务外查询 RWD 并计算当前完整对象、关系、明细和规则。
2. 在删除前完成字段转换、业务键去重和冲突校验。
3. 对象入口短事务替换对象、视图、Key 关联及扩展动作。
4. 有关系所有权的调用方按关系类型调用独立关系入口。
5. 任一写入边界失败时抛出异常并保留该边界上一版数据。

空对象快照只清理当前 Key 关联，不删除对象主表或模型视图。系统不保存一级类型上一版快照，
不计算跨 Key 对象差集，也不扫描无所有权关系做全局孤儿清理。照明关系只按当前 Key 的
新旧回路范围分方向删除和重建。

模板元数据拥有自己的 Key 范围，可以在一级类型全量重建时清理明确失效的模板 Key；
窄范围重建不得扩大为一级类型清理。

## 5. Read Contracts

### 5.1 Object and Model View

对象按 Key 先批量查关联，再按对象 ID 批量查询主表；直接 ID 查询返回实际命中对象。
公共列与 `object_data` 按调用方请求字段重建，单条缺失不清空其他结果。

模型图消费者只查询 `cache_object_model_view`。独立模型视图字段接口 READY 时只投影
`id`、`view_data`，未 READY 时按同一对象 ID 集合实时查询 RWD；真实对象 ID 最后覆盖
历史 JSON 中可能存在的同名值。

### 5.2 Template Detail

模板明细按完整 `(templateKey, objectId, infoCode)` 批量查询。持久化 JSON 不含动态配置，
读取时递归挂回当前配置。缺失业务键只影响对应聚合，新链路不调用 RWD 兜底。

### 5.3 Rules

业务读取直接按项目、对象 ID、信息点编码和规则类型查询 `cache_rule`；
`cache_rule_key_rel` 不参与读取范围。末端、照明未 READY 时实时查询规则，
其他系统继续使用旧规则表。

## 6. READY Lifecycle

状态业务键为 `project_id + CACHE_OBJECT_MODEL_STATUS + child_type`，稳定 MD5 作为
`rc_basics_config.id`。生产只写 READY；历史 `FAILED` 或测试手工非 READY 值仅作为
未就绪兼容值。

单类型构建锁覆盖全部策略、数据库 READY 提交和云端 Redis 刷新：

```text
完整构建成功
  -> 提交 rc_basics_config READY
  -> 刷新云端 Redis
  -> 释放构建锁
```

任何策略返回 `false` 或抛出异常都不改变状态。数据库 READY 已提交但 Redis 刷新失败时，
保留数据库事实并抛出异常，后续启动恢复或再次构建负责传播。

云端启动恢复与边缘启动/30 秒收敛只处理 Redis 缺失 Key，并使用不覆盖写入；
`project.cache.rwd.enabled=false` 时两个自动任务 Bean 均不注册，手动初始化不受影响。

## 7. Whitelist Migration

末端空间范围由 `SPACE_RANGE_TERMINAL` 决定，白名单只维护新版 Key。配置保存先完成
实时对象补全和校验，再在同一短事务中替换新版 Key 与模式配置。

首版升级仅在数据库尚无 `AIR_TERMINAL READY` 且新版 Key 为空时读取一次旧白名单 ID。
有有效旧 ID 时迁移新版 Key 并补写 `WHITE_LIST`；无旧 ID 时按当前壳配置处理。
READY 后永久禁止回读旧白名单残留。

## 8. Superseded Decisions

以下历史方案已被最终实现替代：

- 末端、照明新旧持久化运行结果双写。
- 未 READY 时读取旧版持久化缓存。
- 模型视图按 objType 自动注入固定基础字段。
- `monitoringSurveillanceStatus` 的集合值兼容。
- 独立七表数量核验、差异报告、生产 `FAILED` 和 Debug validate。
- 读取 SQL 按任意 JSON 路径动态投影并按字段签名分组的扩展方案。
- 一级类型快照、跨 Key 差集和全局孤儿对象清理。

这些方案仅保留在 Git 历史和已归档的 13 号取消任务中，不再作为后续维护契约。

## 9. Validation Boundary

已完成编译、聚焦纯单元测试、历史隔离 MySQL Mapper 测试、万级 SQL/索引基准、
Flyway 一致性、静态残留扫描和 `git diff --check`。后续发布仍需在目标环境验证真实
RWD、Redis、XXL-JOB、生产数据库执行计划和云边同步完整性。
