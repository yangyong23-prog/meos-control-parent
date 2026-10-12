# 实施计划：IOT 云端持久化数据查询规则改造

## 实施进度

### 第 1 步：壳配置读取封装（枚举已由同事完成）✅ 已完成

- 枚举定义（同事已完成，直接复用）：`SCENE_PERSISTENT_DATA_RULE`、`GROUP_PERSISTENT_DATA_RULE`、`PERSISTENT_DATA_QUERY_LOGIC`、`PERSISTENT_DATA_TIME_INTERVAL`
- 读取封装：在 `DtpRwdClientServiceImpl` 中以 `shouldUseLatestDataMode()` + `getPersistentDataInterval()` 两个私有方法实现
  - 请求头标识短路：无标识直接返回 false，不读壳配置
  - 无缓存直查 `ShellConfigService.queryByInfoCode`，与"修改后即时生效"场景描述一致
  - 缺失回退默认值、异常回退默认（不抛异常，安全降级）

### 第 2 步：请求头标识读取 ✅ 已完成

- `LocalContextStorage.isRemoveDefaultIpType()`：优先读 HTTP 请求头，无请求头时回退读手动设置（SSE 推送线程）
- `LocalContextStorage.setRemoveDefaultIpType(boolean)`：SSE 推送线程手动设置
- `LocalContextStorage.clearRemoveDefaultIpType()`：SSE 推送线程查询后清理

### 第 3 步：底层 2 个方法判断逻辑 ✅ 已完成

- `DtpRwdClientServiceImpl.queryIpDataOnMoment(4参)` L1162：`shouldUseLatestDataMode()` 为 true → 不设 ipType、时间范围=（now-间隔h ~ now）；否则维持现状（ipType="15min"）
- `DtpRwdClientServiceImpl.queryIpDataOnMomentInOneHour` L1203：同上，维持现状时仍为 15min + (-1h~now)
- 5 参版 `queryIpDataOnMoment`（显式分精度）完全不改动

### 第 4 步：SSE 订阅参数转存 + 推送线程标识恢复 ✅ 已完成

- `SseSubscribeParam` 新增 `removeDefaultIpType` 字段
- `SseEmitterController` 两个订阅方法（GET/POST）检测请求头写入参数
- `CloudIotDataEvent.getSseEmitterObject` 推送前 set ThreadLocal，finally 清理

### 第 5 步：验证与回归 ⏸ 待执行

- [ ] 编译验证（Maven 预存 Lombok 兼容问题，非本次引入）
- [ ] 按 prd.md Acceptance Criteria 逐项自测
- [ ] 回归确认：边缘端、定时任务、显式分精度链路不动

## 改动文件清单

| 文件 | 改动内容 |
|------|---------|
| `LocalContextStorage.java` | 新增 `isRemoveDefaultIpType()`、`set/clearRemoveDefaultIpType()` |
| `DtpRwdClientServiceImpl.java` | 注入 `ShellConfigService`；新增 `shouldUseLatestDataMode()`、`getPersistentDataInterval()`；改造 2 个查询方法 |
| `SseSubscribeParam.java` | 新增 `removeDefaultIpType` 字段 |
| `SseEmitterController.java` | 两个订阅方法中检测请求头写入参数 |
| `CloudIotDataEvent.java` | `getSseEmitterObject` 中 set/clear ThreadLocal |