# 动态查询现状与改造边界

## 代码证据

- common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy/impl/RunningMonitorSecurityStrategy.java:534 的 listAccessSpace 当前先查门禁、批量查规则和空间关系，然后对每个空间再次遍历全部关系设备，并在循环内输出 INFO 日志。空间必须同时命中 accessDemand 和 equipOnSet 规则。
- common/src/main/java/com/persagy/apccontrol/basic/service/security/impl/SecurityServiceImpl.java:1380 的 getEquipmentTypeStatistics 当前按两个 RWD 查询结果分组，之后以全量去重设备 ID 查询四类实时信息点和异常事件；两个异步任务直接写共享 LinkedHashMap，通过 join() 汇合。
- common/src/main/java/com/persagy/apccontrol/basic/service/security/impl/SecurityServiceImpl.java:1496 的 getEquipmentStatisticsVOListMap 对门禁/报警控制器和摄像头分别发起并发 RWD 查询。已有摄像头实时流协议、布撤防能力和监控状态过滤，改造必须保持这些筛选条件。
- common/src/main/java/com/persagy/apccontrol/basic/service/security/impl/SecurityServiceImpl.java:794、:1083 分别批量查询设备/空间实时信息点与异常事件；这些值不能从静态对象缓存恢复。
- common/src/main/java/com/persagy/apccontrol/basic/sse/strategy/security/SecurityEquipmentStatistics.java 直接调用 SecurityService#getEquipmentTypeStatistics；SSE 事件类型、轮询频率和外部数据结构不属于本任务变更范围。

## 可复用缓存契约

- 静态缓存 Key 为 SECURITY_LIST_EQUIPMENT，设备所在空间关系使用 EQUIPMENT_IN_SPACE；关系仓储支持按主体 ID 批量正向查询。
- 静态缓存只提供对象类别、静态能力和关系候选；armingStatus、onlineStatus、runStatus、deployWithdrawAlarmStatus、orderFailAlarm、IoT 规则和异常事件仍必须实时查询。
- 缓存空集合是权威静态结果；动态查询空结果不能被解释为缓存未命中或异常降级。

## 改造边界

1. 通行空间：使用静态设备候选与 EQUIPMENT_IN_SPACE 关系构造一次 spaceId -> flags 聚合，再实时查询两类规则；保留规则分属不同设备、重复关系和空关系语义。
2. 设备统计：复用静态候选完成六类目标设备去重与静态过滤；动态信息点和异常事件各自生成不可变局部结果，主线程汇合后计算统计 VO。
3. 安防异常状态/SSE：只聚合本任务直接涉及的空间和设备对象范围；通用 IoT 查询接口、云边实现、批量信息点和 Redis 中间态补充由其他任务统一优化，本任务不得修改。

## 风险与验证

- 关系方向错误会导致通行空间全部丢失，必须用正向 EQUIPMENT_IN_SPACE 查询和方向断言覆盖。
- 异步查询失败不能静默变成空集合；测试需断言异常传播、取消/超时日志边界和无共享容器竞态。
- 统计结果需覆盖门禁、入侵报警、普通摄像头、具备布撤防能力摄像头及无实时流协议车库摄像头过滤。
