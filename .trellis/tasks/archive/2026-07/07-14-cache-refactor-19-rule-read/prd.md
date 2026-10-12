# IOT 规则读取切换

## Goal

仅切换末端与照明 IOT 规则业务读取到规则主表，键关联只保留写入归属，保持其他系统及云边类型语义不变。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 18 已通过，直接依赖子任务 07、10、15。

## Requirements

- 继承父任务统一异常边界：业务校验和范围冲突使用带中英文资源的 `BusinessException`；JSON 转换统一使用 `ObjectMapperUtils` fail-safe，Converter 不自行捕获 Jackson 异常；读取方对 `null` 整批降级，写入方在删除前拒绝 `null`，Mapper 和事务异常不得吞掉。
- 在 `AbstractCacheIotRuleRepository#mapObjectIdAndInfoCodes` 或等价入口复用子任务 15 的集中路由策略，不复制支持类型或 READY 判断。
- 选中新模型后，按对象 ID、目标信息点和规则类型直接批量查询 `cache_rule`，不得通过 `cache_rule_key_rel` 限定业务读取范围。
- 保持云端 `PERSISTENCE`、边缘端 `TWIN` 的规则类型语义；`cache_rule_key_rel` 只用于单 Key 写入归属、删除和孤儿清理。
- `objectIds` 由调用方保证非空引用；空集合必须立即返回空，不能被解释为查询全部对象。
- 保持返回结果按对象和信息点编码的分组、去重、空结果及共享规则语义。
- 规则主表部分未命中时返回实际命中结果，不混入旧规则表结果；选中新模型后不得请求内回退旧模型。
- 末端、照明未 READY 时实时查询规则，其他运行监测系统继续读取旧规则表。

## Out Of Scope

- 不修改规则稳定 ID、规则写入、孤儿清理或键关联范围替换。
- 不修改对象、模型视图或模板对象明细读取。
- 旧 `cache_iot_rule` 的构建迁移由最终集成方案处理，不在本读取子任务内重复调整。

## Acceptance Criteria

- [x] 规则读取按对象、信息点和规则类型直接批量查询主表，无逐规则查询和 N+1。
- [x] 云端 `PERSISTENCE`、边缘端 `TWIN` 规则类型语义保持不变。
- [x] `objectIds` 为空集合、部分对象和重复对象时按实际主表命中结果返回。
- [x] 共享规则、删除单键、重建单键和全量重建后的主表读取结果正确，关系表只参与写入生命周期。
- [x] 末端、照明 READY 时读取新表、未 READY 时实时查询，其他系统保持旧表。

## Review Gate

完成实现和质量检查后暂停，等待用户审核规则读取语义与系统范围；审核通过前不得开始子任务 20。
