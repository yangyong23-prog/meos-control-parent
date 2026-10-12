# 技术设计：壳配置 same_time 时前端时间参数未生效修复 (UYAI-1348)

## 一、现状与偏差根源

### 1.1 08-26 原设计（预期行为）

`.trellis/tasks/archive/2026-08/08-26-iot-persistence-query-rule-rework/design.md` §2.3：

```
if (ThreadLocal标识 == true && 壳配置查询逻辑 == "latest"):
    ipType 不设置；startTime = now - 间隔h；endTime = now
else:
    维持现状（ipType="15min"；InOneHour 版维持 -1h~now）
```

壳配置语义（`ShellConfigItemEnum`）：`persistent_data_query_logic` 取值 `same_time`（查询同一时刻点固定分精度数据）/ `latest`（查询距离当前最新的数据，不限制分精度）；`persistent_data_time_interval`（1-168h）**仅属于 latest 场景**的时间窗口。

### 1.2 实际实现偏差（commit 4c561d6189）

`DtpRwdClientServiceImpl.queryIpDataOnMoment(4参)` / `queryIpDataOnMomentInOneHour` 分支条件只判断了 `LocalContextStorage.isRemoveDefaultIpType()`（请求头 `removeDefaultIpType=true`），未与壳配置 latest 做与门控：

- 分支内时间一律取壳配置 `[now-interval, now]`（same_time 场景错误消费了 latest 专属的时间间隔配置）
- 分支内不调用 `substitutionParameter`（改造前该方法无条件执行）→ 替换系统白名单场景（冷源/冷热源等运行监测实时页面，extra 中带 `runningMonitorType` + `IPDataStartTime/IPDataEndTime/IPType`）前端时间参数失效
- 分精度依赖 `isKeepDefaultIpType()`（same_time→15min，latest→不传）→ same_time 下把"默认 15min"硬编码与 interval 窗口组合，与改造前行为（无窗口 + 参数可替换）不一致

## 二、修复方案

### 2.1 门控逻辑重组（核心）

两个方法统一改为**双条件门控**：仅 `标识=true && 壳配置=latest` 走最新数据模式；否则完整走改造前逻辑（含 substitutionParameter）。

改造后 `queryIpDataOnMoment(4参)`：

```java
if (LocalContextStorage.isRemoveDefaultIpType() && this.isLatestDataQueryLogic()) {
    // 壳配置为"查询距离当前最新的数据"：时间窗口取壳配置间隔，不传分精度
    LocalDateTime now = DateUtils.now();
    periodDTO.setStartTime(now.minusHours(this.getPersistentDataInterval()).format(DateUtils.DTF_SDF_SECOND));
    periodDTO.setEndTime(now.format(DateUtils.DTF_SDF_SECOND));
} else {
    // 壳配置为"查询同一个时刻点的数据"或未带请求头标识：保持改造前逻辑
    periodDTO.setIpType("15min");
    this.substitutionParameter(periodDTO);
}
```

改造后 `queryIpDataOnMomentInOneHour` else 分支保持改造前现状（15min + -1h~now + substitutionParameter），if 分支同上。

要点：

- `&&` 短路保证：无标识请求不读壳配置（原短路优化保持）；标识=true 但配置非 latest 时只读一次查询逻辑配置即回退旧逻辑，不读 interval
- if 分支内原有的 `isKeepDefaultIpType()` 判断成为死代码（分支进入时配置必为 latest），删除
- latest 场景 `periodDTO.setDataStatus` 等其它字段设置路径不变

### 2.2 辅助方法语义调整

`isKeepDefaultIpType()`（L1578）语义反转并更名：

- 新方法 `isLatestDataQueryLogic()`：壳配置值为 `latest` 返回 true；缺失、未知值、读取异常回退 false（走旧逻辑 = same_time 语义）
- Javadoc 同步改写（原注释描述的是"保持默认分精度"语义，已不适用）
- 类内仅此 2 处调用（L1176/L1223 同文件），无外部引用

`getPersistentDataInterval()` 保持不动（仅 latest 分支使用）。

### 2.3 SSE 推送链路

SSE 推送查询（`CloudIotDataEvent.queryObjectIdAndInfoCodes` L265 等）统一走 `queryIpDataOnMoment(4参)` → 修复自动生效，**无代码改动**。订阅参数转存、`LocalContextStorage.setRemoveDefaultIpType/clearRemoveDefaultIpType` 的 set/remove 生命周期逻辑保持不变，仅纳入回归验证（确认推送线程无 ThreadLocal 泄漏）。

## 三、行为矩阵（修复后）

| 请求头标识 | 壳配置 | ipType | 时间参数 | substitutionParameter |
|---|---|---|---|---|
| true | latest | 不传 | `[now-interval, now]` | 不执行（latest 场景前端不传替换参数） |
| true | same_time/缺失/异常 | 15min | 不设（InOneHour 版 -1h~now） | 执行，命中白名单用前端时间/精度覆盖 |
| 无标识 | 任意 | 15min | 同上 | 执行（改造前现状） |

## 四、关键权衡

| 决策 | 理由 | 权衡 |
|---|---|---|
| 恢复"标识 && latest"双条件门控 | 与 08-26 原设计、PRD 验收标准一致；same_time 语义（固定分精度同刻查询）不消费 latest 专属的 interval 窗口 | 改动需删除 if 内 isKeepDefaultIpType 分支 |
| else 分支完整恢复改造前代码（含 substitutionParameter） | 修复点最小化：前端时间参数对替换系统恢复生效，非替换系统行为不变 | 需回归确认替换系统（冷源/冷热源等）带标识请求行为符合预期 |
| 辅助方法更名 + 反转语义 | 新语义"是否为 latest 逻辑"与调用点可读性一致，避免 `!latest` 双否定 | 单文件 2 处调用点同步修改，无外部影响 |
| 不合并壳配置两次读取 | latest 场景需读查询逻辑 + 时间间隔两个 item（DB 两次查询）；触发频率低（仅 latest+标识 热路径），不做批量查询改造 | same_time/无标识请求保持零读取，不改壳配置读取封装 |

## 五、验证策略

- 静态：编译 + GetProblems 无错误；grep 确认 `isKeepDefaultIpType` 无残留引用
- 本地接口联调矩阵（请求头 × 壳配置 × 是否传替换参数，见 prd.md R2 行为矩阵），核心验证 UYAI-1348 复现场景：same_time + 标识 + 传 `IPDataStartTime/IPDataEndTime` → 请求 DMP 的 startTime/endTime 为前端值
- SSE：订阅带标识，same_time/latest 两种配置下推送查询行为回归
- 防回归：latest + interval=1h/168h 查询窗口正确
