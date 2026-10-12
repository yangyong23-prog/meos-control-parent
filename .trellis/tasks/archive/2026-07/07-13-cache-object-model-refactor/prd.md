# 缓存对象模型重构

## Goal

将对象、对象关系、模型视图、信息点对象明细和 IOT 规则从旧缓存键内的重复 JSON
中解耦，形成可按业务键原子重建、可经既有云边同步分发、可由 READY 状态切换读取的
七表缓存对象模型。

## Completion State

- 20 个子任务的代码与文档均已完成；01～05、13 已先行归档，其余子任务在本轮统一归档。
- 主体实现已进入 `e79be52e5b`，后续性能、路由、字段投影和写入边界修订均已提交。
- 最终公共写入边界由 `39f285d200` 收敛为对象 Key 与通用关系两个独立入口。
- 本任务的代码级验收已完成。真实 RWD、Redis、XXL-JOB 控制台和生产规模数据库验证属于
  发布环境检查，不再作为开发任务保持未归档的理由。

## Requirements

### R1. 数据模型

- 云端和边缘端使用同版本、同内容的 Flyway 创建 `cache_object`、
  `cache_object_key_rel`、`cache_object_rel`、`cache_object_model_view`、
  `cache_template_object_detail`、`cache_rule`、`cache_rule_key_rel` 七张表。
- `cache_object.id` 与 `cache_object_model_view.id` 使用 RWD 对象 ID；其他记录使用
  业务侧生成的稳定 ID，不增加外键、`auto_increment` 或业务组合唯一约束。
- 对象公共字段和模型视图公共字段分别保存为独立列；`object_data` 与 `view_data`
  使用两份独立、代码内固定的 `classCode -> 字段集合` 配置，只保存 RWD 实际返回且
  配置允许的扩展字段。
- `monitoringSurveillanceStatus` 保存为可空单值字符串，查询可以传入多个候选 code，
  但 DTO 恢复仍保持标量语义。
- 模板对象明细按完整 `(templateKey, objectId, infoCode)` 保存自包含递归 JSON；
  动态 `relInfoCodeGroupConfigs` 不落盘，读取时按当前配置递归挂回。
- 规则主表按 `(projectId, objectId, infoCode, type)` 去重；Key 关联只表达写入归属、
  删除和孤儿清理，业务读取直接查询规则主表。

### R2. 写入与重建

- `AIR_TERMINAL`、`LIGHTING` 只构建新版持久化缓存，不再维护
  `cache_rwd_object`、`cache_iot_rule` 等旧版运行结果缓存；其他运行监测系统保持
  既有旧链路。
- 调用方在事务外完成 RWD 查询、字段补齐、关系方向归一化和业务校验；公共写服务不持有
  RWD 客户端，不扩大对象范围或字段范围。
- 对象 Key 入口接收完整 RWD 快照，由统一转换器生成对象与模型视图，并在短事务内替换
  对象、视图、Key 关联及已准备好的明细/规则扩展动作。
- 通用关系使用独立入口，只接收 `DtpRwdRelationTypeEnum` 与对象集合；对象 `id`
  表示关系主体，`relObjIds` 表示完整目标集合。调用方必须先合并同类型、同主体的
  正反向结果。
- 每个新版 Key 独立执行“事务外完整计算、短事务原子替换”；空结果只删除该 Key 的关联。
  不维护一级类型上一版快照，不扫描对象、模型视图或无所有权关系主表做全局孤儿清理。
- 远程计算、转换、业务校验或数据库写入失败时保留对应写入边界的上一版结果，并且不得
  发布新的 READY。

### R3. 读取与兼容

- 新模型固定支持范围只包含 `AIR_TERMINAL`、`LIGHTING`；模板入口还必须满足
  `allowCache()`。
- 支持类型 READY 时只读取新版表，返回实际命中结果；单条缺失不清空同批其他结果，
  也不回退旧表或 RWD。
- 支持类型未 READY、Redis 状态缺失或异常时走实时 RWD/规则查询；其他运行监测系统
  继续使用旧版持久化链路。
- 对象读取按 Key 或对象 ID 批量完成；模型视图只访问
  `cache_object_model_view`，不 JOIN `cache_object`，JSON 字段完全受独立
  classCode Map 约束。
- 模板对象明细按实际命中的完整业务键恢复结果，缺失只影响对应聚合；新链路不在请求内
  调用 RWD 兜底。
- 规则读取按对象 ID、信息点编码和部署端规则类型直接查询规则主表，保持云端
  `PERSISTENCE`、边缘端 `TWIN` 语义。

### R4. READY 与云边边界

- 状态复用 `rc_basics_config`：`type=CACHE_OBJECT_MODEL_STATUS`，
  `child_type` 只允许 `AIR_TERMINAL`、`LIGHTING`；数据库是事实源，Redis 是运行时
  路由状态。
- 单类型全部策略成功后，在同一构建锁内先持久化 READY，再刷新云端 Redis，完成后释放锁。
  构建返回 `false` 或抛出异常时保持原状态，不写 `FAILED`。
- 云端启动恢复和边缘启动/30 秒收敛只处理 Redis 中不存在的 Key，不覆盖已有显式值；
  两端自动任务统一受 `project.cache.rwd.enabled` 条件注册控制。
- 边缘数据库收到 READY 即视为同步完成，不对七表做二次数量或内容核验。数据同步实现和
  READY 到达后的完整性由云边同步项目组负责。
- 只保留手动初始化 XXL-JOB 和当前项目 Debug init；数量核验任务、差异报告、
  Debug validate 及生产 `FAILED` 写入均已取消。

### R5. 性能与工程约束

- 普通主键、对象 ID 和缓存 Key 的数千参数 `IN` 查询使用一次 SQL；复合业务键和关系范围
  按 500 条控制 SQL 长度，缓存 Mapper 多值写入按 1000 条分片。
- 业务校验和范围冲突使用国际化 `BusinessException`；JSON 使用
  `ObjectMapperUtils` fail-safe API；SQL、连接和事务异常不得伪装成缓存未命中。
- 两端 Flyway 必须保持一致；云边数据传输协议、同步顺序和消费实现不属于本项目。

## Acceptance Criteria

- [x] 七张表、索引、稳定主键和云边同版 Flyway 已落地。
- [x] 对象、模型视图、模板明细和规则的存储边界与当前代码配置一致。
- [x] 对象 Key 与通用关系写入接口已拆分，调用方不构造资源层关系范围或持久化实体。
- [x] 单 Key 原子替换、空 Key 清理、失败保旧和无全局孤儿扫描契约已落地。
- [x] 末端、照明只维护新版持久化缓存，未 READY 使用实时链路；其他系统保持旧链路。
- [x] 对象、模型视图、模板明细和规则读取均按实际命中返回且 READY 后不回退。
- [x] READY 提交、启动恢复、边缘收敛和自动任务开关已落地。
- [x] 初始化参数、失败汇总、重跑与状态保持语义已落地。
- [x] 已取消数量核验、生产 `FAILED` 和 Debug validate，并保留 13 号历史记录。
- [x] 编译、聚焦单元测试、历史隔离 Mapper 集成验证、静态检查和文档一致性检查已有记录。
- [x] 20 个子任务均可由当前代码、测试或明确取消决定追溯。

## Release Checks

- 在目标数据库执行 Flyway 并复核最终索引的执行计划。
- 在真实 RWD、Redis 和 XXL-JOB 环境执行初始化与 READY 切换演练。
- 由云边同步项目组验证七表和 `rc_basics_config` 的同步完整性与到达顺序。

这些项目属于部署发布检查，不改变本开发任务已经完成并可归档的结论。

## Out Of Scope

- 云边同步表登记、传输协议、同步顺序和边缘消费实现。
- 物理删除旧表、旧实体和其他运行监测系统的旧链路。
- 新增第三个运行监测系统或动态支持范围。
- 生产规模压测与线上数据校验。
