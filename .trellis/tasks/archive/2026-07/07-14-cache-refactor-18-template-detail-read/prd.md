# 模板对象明细读取切换

## Goal

仅切换信息点对象明细到自包含缓存，并在读取时使用当前动态配置恢复完整结果。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 17 已通过，直接依赖子任务 06、09、15。

## Requirements

- 继承父任务统一异常边界：业务校验和范围冲突使用带中英文资源的 `BusinessException`；JSON 转换统一使用 `ObjectMapperUtils` fail-safe，Converter 不自行捕获 Jackson 异常；读取方对 `null` 整批降级，写入方在删除前拒绝 `null`，Mapper 和事务异常不得吞掉。
- 为 `InfoCodeRwdObjectDetailByTemplateContext` 增加安全的对象预填充入口，不暴露可修改缓存数据的共享引用。
- 在 `RwdObjectDetailInfoCodeAnalyzeStrategy#fillInfoCodeDataModel` 入口复用子任务 15 的集中路由策略，并按当前请求需要的完整 `(templateKey, objectId, infoCode)` 集合执行一次批量缓存查询。
- 按完整复合业务键批量读取并使用实际命中的明细；缺失明细只影响对应聚合，不返回新旧混合结果，也不在请求内调用 RWD 兜底。
- 反序列化完整 `InfoCodeRwdObjectDetailDTO` 树，递归处理任意深度 `relObjects`，收集并预填充所需对象。
- 缓存 JSON 中不应存在 `relInfoCodeGroupConfigs`；读取后实时执行当前配置查询，并递归挂回根对象和所有关联对象。
- 当前动态配置变化必须立即反映到缓存读取结果，且不得回写或污染持久化 JSON、缓存 DTO 或同请求其他聚合结果。
- 保持多模板键、多聚合键、空结果、重复对象和既有结果顺序语义。
- 未选中新模型的末端、照明请求及其他运行监测系统继续使用原读取链路。

## Out Of Scope

- 不修改模板对象明细生成、序列化、范围替换或清理逻辑。
- 不修改对象、模型视图或 IOT 规则读取。
- 不修改动态信息点配置本身，也不增加请求内 RWD 回退。

## Acceptance Criteria

- [x] 已命中的复合业务键恢复对应结果，缺失键使用空结果且不影响其他聚合。
- [x] 任意深度 `relObjects`、多模板键、多聚合键、空结果和重复对象测试通过。
- [x] 测试证明缓存命中链路不调用 RWD 对象或关系查询，缓存缺失时也不在新链路内兜底。
- [x] 动态配置在每次读取时递归挂回，配置变化可见，持久化 JSON 和缓存 DTO 不被修改。
- [x] 未选中新模型及其他系统行为不变；本任务 diff 只包含模板明细读取接入和聚焦测试。

## Review Gate

完成实现和质量检查后暂停，等待用户审核整批命中与动态配置恢复；审核通过前不得开始子任务 19。
