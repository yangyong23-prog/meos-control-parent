# Research: 末端白名单首版迁移判定

- Query: `CacheRwdAirTerminalWhiteListStrategy#buildCacheData` 当前构建白名单的逻辑是否保持历史语义；首版升级时应如何确定白名单模式和对象 ID。
- Scope: internal
- Date: 2026-07-23

## Findings

### 结论

当前逻辑不满足首版升级兼容要求。

新流程完成迁移以后，`SPACE_RANGE_TERMINAL` 可以作为当前模式事实源，新版
`WHITELIST_AIR_TERMINAL_space` Key 关联可以作为对象 ID 事实源；但首版迁移以前，历史项目的
模式和对象 ID 都只由 `cache_rwd_object` 中该白名单 Key 是否有行表达。当前代码先要求壳配置为
`WHITE_LIST`，再读取历史行，导致未持久化壳配置的历史白名单项目直接跳过迁移。

### 历史真实语义

- 主会话通过 Git 历史确认：commit `81087350ca`（`UYAI-380`）中的
  `CacheRwdAirTerminalWhiteListStrategy#buildCacheData` 先按白名单 Key 查询
  `cache_rwd_object`；查询结果为空时返回 `false`，非空时才处理并保存白名单。
- 历史 Key 由 `WHITELIST_ + runningMonitorType.name() + '_' + objectType.value()` 生成，末端空间
  的实际值是 `WHITELIST_AIR_TERMINAL_space`。当前常量值仍相同
  （`CacheObjectModelKeyConfig.java:46-47`），因此旧行可以直接作为首版迁移来源。
- 换言之，在旧版本中，“该 Key 有 `cache_rwd_object` 行”同时表达：
  1. 项目启用了末端白名单模式；
  2. 这些行的 `object_id` 是白名单范围。
- 当前先读壳配置的 gate 由 commit `d1615195eb`（`UYAI-1142`）引入，属于新版模式管理，不能
  反向作为识别旧版数据的前置条件。

### 当前模式事实源与 ID 事实源

迁移前后必须分开：

| 阶段 | 模式事实源 | 对象 ID 事实源 |
| --- | --- | --- |
| 历史版本 / 首版迁移前 | 旧 `cache_rwd_object` 白名单 Key 是否有行 | 同一批旧行的 `object_id` |
| 新版保存完成后 | `SPACE_RANGE_TERMINAL` 的 `FULL` / `WHITE_LIST` 值 | `cache_object_key_rel` 中新版白名单 Key 的关联 |

当前新版保存链路已经把两份事实原子维护：

- 保存自定义白名单时，`CacheRwdServiceImpl.java:327-341` 先取得配置 ID，再调用带提交回调的
  `replaceWhitelist`；回调把壳配置写为 `WHITE_LIST`。
- 切换全量或删除时，`CacheRwdServiceImpl.java:287-297`、`387-396` 在同一事务中把壳配置写为
  `FULL` 并删除新版 Key。
- 通用壳配置保存为全量时，`ShellConfigController.java:88-96` 转入
  `saveShellConfigAndRemoveAirTerminalWhitelist`；后者在
  `CacheRwdServiceImpl.java:407-421` 中保存完整壳配置并删除新版 Key。
- 消费端已经按壳配置选择范围：`AirTerminalQuerySpace.java:116-140` 先决定白名单或项目全量 Key，
  不再根据 Key 是否命中反推模式。

因此，完成首版迁移后继续让壳配置作为模式事实源是正确的；错误只在于当前代码要求这个“新版
事实”必须在迁移开始前已经存在。

### 当前迁移分支为什么不可达

`CacheRwdAirTerminalWhiteListStrategy.java:96-115` 的顺序是：

1. 先调用 `isWhitelistSpaceRangeConfigured()`；
2. 非白名单立即以空集合替换新版 Key；
3. 只有已判定为白名单，且新版 Key 为空、数据库无 READY 时，才读取旧
   `cache_rwd_object`。

而 `ShellConfigServiceImpl.java:1181-1210` 明确在项目没有对应 `rc_basics_config` 记录时返回枚举
默认配置；`ShellConfigItemEnum.java:840-841` 的默认值是 `FULL`。所以典型历史白名单项目会得到：

```text
旧白名单行存在 + 壳配置从未持久化
    -> queryByInfoCode 返回默认 FULL
    -> buildCacheData 在第 98 行判定非白名单
    -> 第 100 行清空新版白名单 Key
    -> 第 103 行历史迁移分支永远不会执行
```

如果末端其余策略成功，`CacheRwdDomainServiceImpl.java:171-178` 仍会提交 `AIR_TERMINAL READY`。
随后 `shouldMigrateLegacyWhitelist()` 因数据库已经 READY 永久返回 `false`
（`CacheRwdAirTerminalWhiteListStrategy.java:118-130`）。旧表虽然仍有历史行，新版业务也不会再迁移
或消费这些行。

### 首次迁移的正确顺序

最小兼容顺序应是：

1. 读取数据库 `AIR_TERMINAL READY`，不能用 Redis 状态。当前
   `getPersistedReadyTime(AIR_TERMINAL)` 的选择正确；其实现只在数据库存在有效 READY 时返回时间
   （`CacheObjectModelStatusServiceImpl.java:150-168`）。
2. 读取新版白名单 Key 的对象 ID。
3. 当“数据库未 READY 且新版 Key 为空”时，先读取旧 `cache_rwd_object` 白名单 Key。
4. 旧行非空时，按历史语义直接判定为白名单迁移，不再要求
   `SPACE_RANGE_TERMINAL=WHITE_LIST`；按旧 `object_id` 实时查询 RWD。
5. 在同一个短事务中同时替换新版白名单 Key，并把 `SPACE_RANGE_TERMINAL` 持久化为
   `WHITE_LIST`。只有两项都成功才允许本类型最终进入 READY。
6. 不满足首版迁移条件时，再按壳配置执行新版日常逻辑：`WHITE_LIST` 使用新版 Key 当前 ID；
   `FULL` 清空新版白名单 Key。

必须同步持久化 `WHITE_LIST`。如果只迁移新版 Key 而不写模式：

- 当前请求和后续消费仍会按默认 `FULL` 读取项目全量 Key；
- 下一次构建又会进入第 98-100 行，把刚迁移的新 Key 清空；
- 因此“迁移成功但业务无效”，并且 READY 会阻止再次读取历史行。

现有事务能力可以直接支持该修复：

- `CacheRwdAirTerminalWhiteListStrategy.java:198-210` 已有带 `afterReplaceAction` 的
  `replaceWhitelist` 重载；
- `CacheObjectModelWriteServiceImpl.java:135-147` 在缓存 Key 写入后、同一事务内执行本地回调；
- 回调返回 `false` 时，`CacheObjectModelWriteServiceImpl.java:455-472` 会回滚整个事务。

因此不需要新增迁移表、双写旧表或额外事务框架。只需调整首版判定顺序，并复用现有壳配置保存
逻辑把 `WHITE_LIST` 与新版 Key 一起提交。RWD 查询仍应保留在事务外；RWD 或转换失败时不能改变
壳配置、新版 Key 或 READY。

### 清空数据与状态后的行为

| 清空范围 | 当前行为 | 修复后行为 |
| --- | --- | --- |
| 清空新版七表和 READY，保留旧白名单行 | 壳配置缺失时按默认 FULL，跳过迁移 | 旧行非空，迁移 ID 并原子写 `WHITE_LIST` |
| 清空新版七表和 READY，壳配置已是 `WHITE_LIST`，保留旧行 | 可以进入当前迁移分支 | 同样可以迁移 |
| 只清 READY，新版 Key 仍有 ID | 按壳配置重建 | 保持新版日常逻辑 |
| 新旧白名单对象行和 READY 全部清空 | 没有任何地方可以恢复白名单 ID | 同样无法恢复；必须重新保存白名单配置或引入独立配置表 |
| 错误构建已经写入 READY、旧行仍在 | 后续不再迁移 | 修复代码后还需清除该项目末端持久化/Redis READY，再重跑一次初始化 |

当前设计把新版 Key 关联同时当作白名单 ID 的持久化配置。若测试把新旧缓存行全部清空，壳配置
只剩“使用白名单”这一模式值，不包含具体对象 ID，任何构建入口都无法重新计算出用户曾经选择的
对象。要让白名单在清空所有缓存表后仍可重建，需要把 ID 另存到非缓存配置表；这不是本次最小
兼容修复。

### 与数量差异的关系

- 该缺陷会直接导致 `WHITELIST_AIR_TERMINAL_space` 对应的
  `cache_object_key_rel` 数量为零，并导致白名单业务返回全量或空范围。
- 白名单空间通常也是项目全量空间的子集，`cache_object` 和
  `cache_object_model_view` 以对象 ID 主键复用记录，因此它不一定造成对象主表同等幅度的数量差。
- 定时初始化和 `/cacheRwd/triggerBuildCache` 在显式传入 `AIR_TERMINAL` 时最终都调用
  `CacheRwdDomainService#buildCache(AIR_TERMINAL)`，会共享同一个错误，不应在完全相同的项目、类型和
  初始快照下单独产生不同白名单结果。
- 两条入口的范围仍不同：任务会遍历项目并逐个支持类型调用
  （`CacheObjectModelInitTaskHandler.java:138-177`），接口只使用当前请求上下文和传入类型
  （`CacheRwdController.java:43-46`）。接口类型为空时，
  `CacheRwdDomainServiceImpl.java:122-145` 还会按策略 Bean 重复触发同一一级类型。大范围总数差异需要
  与白名单迁移缺陷分开处理。

### Files Found

- `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/strategy/impl/CacheRwdAirTerminalWhiteListStrategy.java`：当前白名单完整构建、旧行迁移和新版 Key 替换。
- `common/src/main/java/com/persagy/apccontrol/basic/service/shellconfig/ShellConfigServiceImpl.java`：壳配置缺失时返回枚举默认值。
- `common/src/main/java/com/persagy/apccontrol/basic/model/enumtype/ShellConfigItemEnum.java`：末端空间范围默认值为 `FULL`。
- `common/src/main/java/com/persagy/apccontrol/basic/service/cacherwd/CacheRwdServiceImpl.java`：新版白名单保存、删除及模式持久化链路。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/service/CacheObjectModelWriteServiceImpl.java`：新版 Key 与本地回调的短事务边界。
- `common/src/main/java/com/persagy/apccontrol/basic/service/dashboard/space/AirTerminalQuerySpace.java`：业务读取以壳配置选择白名单或全量范围。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/service/CacheRwdDomainServiceImpl.java`：构建调度、READY 提交和空类型全量构建行为。
- `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/cacheobjectmodel/CacheObjectModelInitTaskHandler.java`：定时任务的集团、项目和类型执行范围。

### Related Specs

- `.trellis/spec/backend/cache-object-model.md:268-270` 当前把“壳配置明确为白名单”写成首版迁移前提；
  该前提与已确认的历史事实源冲突，需要在实现修复时同步调整。
- `.trellis/tasks/07-14-cache-refactor-20-integration/prd.md:23,55` 和
  `.trellis/tasks/07-14-cache-refactor-20-integration/design.md:18` 包含相同前提，需要由主任务按本次确认
  更新后再实现。
- `.trellis/spec/backend/cache-object-model.md:268` 关于迁移完成后以壳配置作为唯一模式事实、模式与
  新版 Key 原子提交的约束仍然成立。

### External References

无。结论完全来自仓库代码、Trellis 契约以及主会话核对的仓库历史。

## Caveats / Not Found

- 本研究未连接真实数据库，也未执行测试。
- 研究代理按职责未执行 Git 命令；历史方法和提交号由主会话通过 `git show` 核实后提供。
- 当前没有独立非缓存表保存白名单对象 ID。若产品要求“清空新旧所有缓存表后仍自动恢复白名单”，
  需要新增持久化配置模型，不能靠调整构建顺序实现。
