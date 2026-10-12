# 动态查询与统计设计

## 1. 数据流与边界

静态 SECURITY Key 和 EQUIPMENT_IN_SPACE 关系提供设备、空间候选及关系索引；实时规则、IoT 值和异常事件分别查询后生成局部结果，最终汇合到旧 DTO、VO 和 SSE 契约。

静态层只决定“查询谁”和“关系如何映射”；实时层决定规则、状态和异常值。任何实时查询失败都沿现有异常链路向上，不得用静态值或空列表掩盖。

## 2. 通行空间聚合

1. 从 SECURITY_LIST_EQUIPMENT 取得门禁候选；从 EQUIPMENT_IN_SPACE 正向批量读取 equipmentId -> spaceIds。
2. 按设备 ID 建立两个规则标志集合：实时规则包含 accessDemand 的设备集合、包含 equipOnSet 的设备集合。
3. 单次遍历关系边，将目标空间聚合为两个布尔标志；同一空间由不同设备提供两个规则时仍保留，重复边只计一次。
4. 输出两个标志均为真的空间，保持旧返回 DTO、顺序尽可能稳定，并删除逐空间 INFO 日志。

未 READY 时沿现有 RWD 路径；READY 的静态候选为空仍视为权威空范围，不回查 RWD。缓存仓储异常和动态规则查询异常直接传播。

## 3. 设备统计

1. 在 SecurityServiceImpl#getEquipmentStatisticsVOListMap 中保留六类目标设备的静态过滤条件，READY 时从静态候选读取项目范围；楼层视角只对候选 ID 做关系/空间范围收窄，未 READY 时保持原 RWD 路径。
2. 对最终去重 ID 一次查询四类动态状态信息点；异常事件单独按同一 ID 集合查询。
3. 每个异步任务只返回独立局部结果，例如 Map<SecurityEquipmentStatusEnum, Set<String>> 与异常 ID 集合；主线程在 allOf 后合并，避免并发写共享 LinkedHashMap。
4. 统计计算继续复用现有 mapToStatusEnum、分类处理和零值语义；不把动态结果写入缓存。
5. 只有性能采样证明单次下游请求过大时，才在现有有界执行器上增加固定上限分批，并对每批异常、超时和取消做可观察记录。

## 4. 安防异常状态与 SSE

- 安防异常状态按空间和设备分别聚合对象范围，多设备类别 ID 一次去重后调用既有实时服务。
- 安防异常状态、异常事件和 SECURITY_EQUIPMENT_STATISTICS 只消费现有实时服务结果；不修改通用 IoT 查询接口、实现、SseEmitterEventEnum、轮询频率或外部数据结构。
- AbnormalState 等通用 SSE 捕获异常时，需保持现有事件框架行为，同时让异常日志包含项目、事件和查询范围，不将异常伪装成正常空数据。

## 5. 兼容性与回滚

- 对外接口路径、请求参数、响应字段和状态枚举不变。
- 实现集中在安防服务/策略及必要的 SSE 策略；若性能或语义回归，可按方法级回退到原 RWD/聚合实现，不涉及缓存数据迁移。
- 动态字段不得进入 cache_object、cache_object_model_view 或长期业务缓存；静态缓存规范继续作为边界检查依据。

## 6. 风险控制

- 关系方向：只使用 EQUIPMENT_IN_SPACE 的正向 objectId -> relObjectId 语义并增加方向测试。
- 线程安全：禁止异步任务直接写共享可变 Map/List；使用局部返回值和主线程合并。
- 异常语义：CompletableFuture.join() 的包装异常需保留根因并记录结构化上下文；超时/取消不得被当作空查询结果。
- 实时性：不得给动态方法增加 @Cacheable 或复用静态对象快照中的状态字段。
