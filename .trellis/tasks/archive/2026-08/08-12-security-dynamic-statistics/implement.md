# 动态查询与统计实施计划

## 实施顺序

1. [ ] 激活任务前复核 prd.md、design.md 和研究记录，确认静态 Key、关系方向、六类统计候选和动态字段边界。
2. [ ] 重写 RunningMonitorSecurityStrategy#listAccessSpace：消费静态候选/关系，批量查询实时规则，单次聚合空间标志；保留未 READY 原 RWD 路径和旧空值语义。
3. [ ] 重构 SecurityServiceImpl#getEquipmentStatisticsVOListMap 与 getEquipmentTypeStatistics：收窄静态候选和动态信息点 ID，局部结果汇合，移除共享 Map 并发写；保持分类、过滤和状态映射。
4. [ ] 优化安防异常状态 SSE 的空间/设备对象范围聚合；不修改通用 IoT 查询接口、实现、batchQueryInfoCode 或 rcRunStatus 补充逻辑。
5. [ ] 增加聚焦单元测试：规则分属不同设备、重复/空关系、项目隔离、缓存空范围、调用计数、统计分类、动态异常传播和 SSE 对象范围去重。
6. [ ] 执行质量检查、模块编译和性能采样；记录每个动态下游调用的对象数、信息点数、批次数、耗时和异常情况。

## 重点文件

- common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy/impl/RunningMonitorSecurityStrategy.java
- common/src/main/java/com/persagy/apccontrol/basic/service/security/impl/SecurityServiceImpl.java
- common/src/main/java/com/persagy/apccontrol/basic/service/common/impl/RunningMonitorServiceImpl.java
- common/src/main/java/com/persagy/apccontrol/basic/sse/strategy/security/SecurityEquipmentStatistics.java
- common/src/main/java/com/persagy/apccontrol/basic/sse/strategy/common/AbnormalState.java（仅在证据证明需要时）

## 验证命令

    mvn -pl common '-Dtest=*Security*Test,*RunningMonitor*Test,*InfoCode*Test' test
    mvn -pl common -DskipTests compile
    git diff --check HEAD
    python .\.trellis\scripts\task.py validate 08-12-security-dynamic-statistics

性能采样至少覆盖：通行空间关系边数量与规则查询次数、设备统计唯一设备数/动态查询次数、异步任务耗时/异常，以及异常状态唯一对象数。数据库/共享资源验证由主会话串行执行。

## 回滚点

- 通行空间聚合可单独恢复 RunningMonitorSecurityStrategy#listAccessSpace 的原 RWD 实现。
- 设备统计可单独恢复 getEquipmentStatisticsVOListMap 和动态状态汇合实现。
- 异常状态 SSE 聚合若改变实时语义，只回退对应方法，不回退静态缓存提交。

## 高风险项

- EQUIPMENT_IN_SPACE 关系方向或楼层视角范围错误。
- 动态异常被 join() 包装后静默转换为空集合。
- 统计状态映射、摄像头实时流协议过滤和零值展示回归。
- 为追求吞吐引入无界并发或把动态结果写入缓存。
