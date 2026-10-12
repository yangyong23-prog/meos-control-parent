# 对象缓存读取切换

## Goal

切换对象按缓存键和直接 ID 的批量读取，保持批量查询、部分命中结果语义和
READY 后不做请求内回退的约束；支持类型未 READY 时由上层走实时查询。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 15 已通过，直接依赖子任务 02、03、08、15。

## Requirements

- 继承父任务统一异常边界：业务校验和范围冲突使用带中英文资源的 `BusinessException`；JSON 转换统一使用 `ObjectMapperUtils` fail-safe，Converter 不自行捕获 Jackson 异常；读取方对 `null` 整批降级，写入方在删除前拒绝 `null`，Mapper 和事务异常不得吞掉。
- 在 `CacheRwdDomainService`、`BaseCacheRwdStrategy` 或等价对象读取入口接入子任务 15 的集中路由策略，不复制支持类型、`allowCache()` 或 READY 判断。
- 选中新模型后，按缓存键先批量查询 `cache_object_key_rel`，再按固定批大小查询 `cache_object`；直接 ID 查询只批量访问 `cache_object`。
- DTO 公共属性只从对象表独立列重建，再合并 `object_data` 扩展字段；扩展 JSON 不得覆盖公共属性。
- 独立列中的单个 `monitoringSurveillanceStatus` code 必须恢复为 DTO 同名动态属性；业务传入多个候选状态码时使用 `IN` 按命中任意一个过滤。
- 新链路恢复状态后，`DtpRwdObjectDTO#isControlled()` 保持既有标量语义；
  状态字符串为 `2` 或 `3` 时视为可控，不增加集合值兼容分支。
- 保持旧接口对空入参、去重、顺序或集合、过滤条件和空结果的既有契约。
- 直接 ID 查询按请求 ID 批量读取并返回实际命中的对象，不重复校验请求 ID 与命中 ID 集合是否完全一致。
- 按缓存键查询到关联但对象主表缺少记录时跳过对应对象，其他对象正常返回；选中新模型后不得混读旧表或调用 RWD 兜底。
- 名称、存在性、数量等轻量查询只投影需要的列，不得无条件加载或反序列化 `object_data`。
- 末端、照明未 READY 时由上层实时查询 RWD；其他运行监测系统继续使用原读取链路。

## Out Of Scope

- 不修改模型视图、模板对象明细或 IOT 规则消费者。
- 不修改对象写入、初始化、核验、状态收敛或集中路由判定规则。
- 不删除旧对象表、实体或仓储。

## Acceptance Criteria

- [x] 按缓存键读取通过关联表和对象主表批量完成，无逐对象查询和 N+1。
- [x] 直接 ID 和按缓存键读取均覆盖全命中、部分缺失、空入参、重复 ID 与几千对象 ID 单次批量查询。
- [x] 公共列与 `object_data` 可正确重建 DTO，扩展字段不能覆盖公共列，按需投影不会加载无关 JSON。
- [x] 单值状态可恢复到 DTO，并覆盖单 code、多个候选 code、空条件和 SQL `NULL` 的过滤行为。
- [x] `isControlled()` 覆盖标量 `2/3`、其他字符串和空值，不引入多值格式。
- [x] READY 后部分缺失返回实际命中对象且不访问旧表或 RWD；支持类型未 READY 走实时路径，其他系统行为不变。
- [x] 本任务 diff 只包含对象读取接入、必要仓储查询调整和聚焦测试。

## Review Gate

完成实现和质量检查后暂停，等待用户审核对象读取完整性与无回退语义；审核通过前不得开始子任务 17。
