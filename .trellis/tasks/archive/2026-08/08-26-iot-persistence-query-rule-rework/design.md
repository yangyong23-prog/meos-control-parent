# 技术设计：IOT 云端持久化数据查询规则改造

## 一、架构与边界

改造集中在 4 个层面，全部在 common 模块：

```
前端请求头 removeDefaultIpType=true
        │ (HTTP 请求线程)
        ▼
LocalContextStorage.REQUEST (TransmittableThreadLocal<HttpServletRequest>)
        │                                          ▲
        │ (异步线程池 ttlExecutor 可传递)           │ SSE 推送线程手动 set/remove
        ▼                                          │
DtpRwdClientServiceImpl.queryIpDataOnMoment(4参) / queryIpDataOnMomentInOneHour
        │ (统一判断点：请求头标识 + 壳配置)
        ▼
壳配置读取封装 (读 group_cloud_persistent_query 2 个配置项，带缓存，缺失回退默认)
```

- **不改**：5 参版 queryIpDataOnMoment（显式 ipType）、queryIPDataInPeriod、listInfoCodeValueByCloud 等显式分精度链路
- **不改**：约 136 处调用点（统一收敛在底层 2 个方法）
- **不在范围**：控制服务 dtp-meos-server、边缘端（无该壳配置 → 默认 same_moment 行为不变）、前端 UI

## 二、数据流与契约

### 2.1 请求头标识读取

- 请求头：`removeDefaultIpType`，值必须为 `true`
- 读取点：`LocalContextStorage.REQUEST`（TransmittableThreadLocal，已有）新增便捷方法（如 `isRemoveDefaultIpType()`）
- TransmittableThreadLocal 保证 BusinessThreadPoolExecutor 异步任务可传递；定时任务线程无上下文 → 标识为空 → 不生效（符合预期）
- SSE 推送线程：CloudIotDataEvent L258 查询前，把订阅参数中的标识手动 set 到 ThreadLocal，查询完成后 finally remove（防线程污染）

### 2.2 SSE 订阅参数转存

- `SseSubscribeParam` 新增字段 `removeDefaultIpType`（Boolean）
- 订阅建立时（SseEmitterController HTTP 线程内）检测请求头，`=true` 则写入订阅参数
- 推送线程（@Scheduled 定时任务，无 HTTP 上下文）查询前按订阅参数恢复标识到 ThreadLocal

### 2.3 底层 2 个方法判断逻辑

```
queryIpDataOnMoment(4参) / queryIpDataOnMomentInOneHour:
  if (ThreadLocal标识 == true && 壳配置查询逻辑 == "latest"):
      ipType 不设置；startTime = now - 间隔h；endTime = now
  else:
      维持现状（ipType="15min"；InOneHour 版维持 -1h~now）
```

- 间隔 h 来自壳配置 `persistent_data_time_interval`（默认 1，范围 1-168，非法值回退默认并 warn）
- 查询逻辑配置 `persistent_data_query_logic`：`same_time`（默认，行为不变）/ `latest`

### 2.4 壳配置读取封装（枚举已由同事完成，直接复用）

同事已完成的枚举定义：
- `ShellConfigSceneEnum.SCENE_PERSISTENT_DATA_RULE("persistent_data_rule", ...)`，描述"修改后即时生效"
- `ShellConfigGroupEnum.GROUP_PERSISTENT_DATA_RULE("group_persistent_data_rule", ...)`
- `ShellConfigItemEnum.PERSISTENT_DATA_QUERY_LOGIC`（code `persistent_data_query_logic`，默认 `same_time`，可选 `latest`）
- `ShellConfigItemEnum.PERSISTENT_DATA_TIME_INTERVAL`（code `persistent_data_time_interval`，默认 1h，范围 1-168）
- `BasicsConfigTypeEnum.SHELL_CONFIG_PERSISTENT_DATA_RULE` 映射已建

读取封装（本任务待做）：
- 参照 CacheRwdServiceImpl.getConfiguredWhitelistType L478-501 模式：读 2 个配置项、缺失回退默认值、未知值 warn + 回退默认
- **无缓存直查**：场景要求"修改后即时生效"，与现有壳配置读取模式一致（queryShellConfigByScene L280 直查 DB），不加 TTL 缓存
- **短路优化**：底层方法先判断请求头标识（ThreadLocal 零成本），无标识直接走现状分支不读壳配置；仅前端加标识的 17 个接口触发壳配置读取，DB 开销可接受

## 三、兼容性与迁移

- 三条件门控：无标识 / 标识非 true / 壳配置非 latest_data → 行为与改造前完全一致
- 边缘端：无该壳配置（或未配置）→ 回退默认 same_moment → 行为不变
- 定时任务（空间分析 ×2、ObjectInfoCodeGroupTask 无标识时）：无 ThreadLocal 标识 → 行为不变
- 显式分精度查询（DAY、1d、5min、listInfoCodeValueByCloud 等）：完全不动
- 接口入参、响应格式不变；请求头为新增项，老前端不带则不受影响

## 四、关键权衡

| 决策 | 理由 | 权衡 |
|------|------|------|
| 判断点收敛在底层 2 个方法 | 136 处调用点零改动，回归面最小 | 底层方法需依赖 ThreadLocal + 壳配置读取，耦合度略增 |
| 壳配置无缓存直查 + 请求头短路 | 场景要求"修改后即时生效"；无标识请求零开销直接短路 | 有标识的 17 个接口每次多一次 RcBasicsConfig 查询（可接受） |
| SSE 推送线程手动 set/remove ThreadLocal | 推送无 HTTP 上下文，只能靠订阅参数恢复 | finally remove 是硬约束，防线程池污染 |
| 空调系列本次不加标识 | 产品"暂不改动"，后端无需模块开关 | 后续启用需前端补加标识（工作量在前端） |

## 五、运维与回滚

- 壳配置默认值即现状 → 代码上线后行为不变；测试项目切 latest_data 验证后放开
- 回滚：壳配置页面切回"查询同一个时刻点的数据"即回滚，无需发版
- 观察点：RWD 查询耗时（大间隔时时间范围扩大）、壳配置缓存命中
