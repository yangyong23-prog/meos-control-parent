# 缓存对象模型集成基准结果

## 环境

- 数据库：MySQL `8.0.46`
- 容器：`codex-cache-object-model-mysql`
- Schema：`cache_model_test`
- 迁移：`saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.1__cache_object_model.sql`
- 数据范围：单项目 `benchmark-project`，20 个模板键，末端/照明两类运行监测类型

> `cache_rule_key_rel.running_monitor_type`、`running_monitor_second_type` 及相关索引组成已在
> 后续设计收敛中删除，规则范围改为仅由 `cache_key` 表达。本文中涉及这些字段、
> `idx_running_monitor_type`、普通索引总数以及运行监测类型规则查询的实测结论均属于旧结构
> 结果，不能用于评价当前结构；后续统一性能验证时需要基于当前迁移重新测量。此次调整未连接
> 数据库，因此不改写历史实测数值。

## 数据量与批次

> 本文记录的是写入分片仍为 500 条、普通 `IN` 查询也按 500 条分片时完成的历史基准。
> 后续代码已将 `CacheBatchExecutor` 的缓存 Mapper 多值写入上限统一调整为 1000 条，
> 并将最多几千个参数的普通主键、对象 ID 和缓存 Key `IN` 读取改为一次 SQL；模板明细、
> 规则完整业务键及关系范围等复合 `OR` 条件仍保持 500 条。本次调整未重新连接数据库或改写以下历史测量结果。

基准数据通过一个事务写入，使用递归序列生成对象和关联，包含共享对象与共享规则引用：

| 表 | 记录数 | 500 条批次数 |
| --- | ---: | ---: |
| `cache_object` | 10,000 | 20 |
| `cache_object_key_rel` | 11,000 | 22 |
| `cache_object_rel` | 10,000 | 20 |
| `cache_object_model_view` | 10,000 | 20 |
| `cache_template_object_detail` | 20,000 | 40 |
| `cache_rule` | 10,000 | 20 |
| `cache_rule_key_rel` | 12,000 | 24 |

初始装载事务耗时 `2,604.897 ms`。对七张表执行全量更新时间并在事务中保持 5 秒后提交，事务耗时 `5,366.306 ms`；Docker 监测到的内存峰值约 `422.5 MiB / 3.822 GiB`（10.79%）。该内存值是 MySQL 容器观测值，不代表应用 JVM 峰值。

## 迁移结构检查

迁移在已有七表和索引的 Schema 中再次执行成功，幂等检查通过。`information_schema` 实测结果：

- 目标表 `7` 张，包含主键的目标表 `7` 张。
- 普通索引 `15` 个。
- 外键 `0`、业务唯一约束 `0`、`auto_increment` 列 `0`。
- `cache_object.local_name`、`monitoring_surveillance_status`、`object_data` 允许 `NULL`。
- `cache_object_model_view.local_name`、`view_data` 允许 `NULL`。
- `cache_template_object_detail.result_json` 为 `JSON NOT NULL`。

## SQL 长度与执行计划

以下结果来自 `EXPLAIN ANALYZE`，SQL 使用生产代码的 500 条分片规模构造；`actual_end_ms` 为 MySQL 报告的实际执行结束毫秒数，未包含客户端进程启动开销。

| 查询 | SQL 长度 | 关键索引 | 实际行数 | actual_end_ms |
| --- | ---: | --- | ---: | ---: |
| 对象键关联按 20 个缓存键 | 410 | `idx_cache_key` | 11,000 | 14.2 |
| 对象主表按 500 个对象 ID | 6,087 | `PRIMARY` | 500 | 0.386 |
| 关系正向按 500 个主体 | 6,141 | `idx_object` | 500 | 2.27 |
| 关系反向按 3 个目标 | 181 | `idx_rel_object` | 300 | 0.885 |
| 模型视图按项目和楼层 | 177 | `idx_floor` | 100 | 0.152 |
| 模板明细 500 个完整业务键 | 40,082 | `idx_template_object_info` | 500 | 20.4 |
| 规则 500 个完整业务键 | 34,326 | `idx_project_object_rule` | 500 | 22.8 |
| 旧结构：规则关联按 20 个缓存键、一级类型 | 470 | `idx_cache_key` 后过滤 | 5,000 返回（10,000 扫描） | 14.7 |
| 旧结构：规则关联按一级类型分页 | 136 | `idx_cache_key` 后过滤并排序 | 500 返回（12,000 扫描） | 6.45 |

上述两条一级类型规则查询及 `idx_running_monitor_type` 选择结论仅适用于旧结构，当前结构已
删除该字段和索引。规则引用检查按 `rule_id` 使用 `idx_rule`、对象引用检查按 `object_id`
使用 `idx_object` 的历史结果予以保留，但仍需在后续统一性能验证时结合当前索引定义复测。

历史基准中的 500 条查询分片将最长的模板明细查询限制在约 40 KB，低于常见 `max_allowed_packet` 限制；当前仅模板明细、规则完整业务键及关系范围等复合 `OR` 条件继续保持 500 条，普通 `IN` 读取已经改为一次 SQL，缓存 Mapper 多值写入已统一调整为 1000 条。历史测量没有发现逐对象查询；本次读取与写入分片调整未重新执行数据库基准。

最终回归另覆盖模板元数据重建范围：单一级类型全量查询为空时清理该类型旧键，带设备类的窄范围查询为空时不扩大清理；信息点导入在删除源模板前按完全相同的范围捕获旧模板 ID，并按 ID 精确删除旧缓存键。

## 边界说明

- 本测量只验证云端七表结构、索引、批量 SQL 和数据库执行计划。
- 云边同步、边缘表复制及同步完成感知属于其他项目组；本项目仅消费边缘数据库同步到的 `READY` 状态。
- 另使用隔离 MySQL `8.0.46` 容器执行 `CacheObjectModelMapperIntegrationTest`：3 个用例通过，覆盖七表真实 Mapper 批量读写和精确查询、监测监控状态单值过滤、关系范围替换隔离，以及最后一张表写入失败时七表事务整体回滚。
- 该基准不等同于真实业务 Spring Boot 集成压测；真实 RWD、Redis、XXL-JOB 调度和线上消费者对账仍需在具备外部服务的部署环境验收。
- 未执行生产规模压测；10,000 条数据仅用于 SQL 和索引级验证，不代表生产吞吐指标。
- 测量及 Mapper 集成测试完成后已删除临时容器 `codex-cache-object-model-mysql` 和 `codex-cache-object-model-mysql-final`，未操作项目已有数据库容器或数据。
