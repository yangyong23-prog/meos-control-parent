## Context

穹顶通风 22 个壳配置参数（`group_control_vent_dome`，项目级，无 objectId）已由 PRD-260902 落地配置与保存能力，本期将其同步至 DTP 孪生服务。

与既有同步方法的差异：
- `saveFreqRangeConf()`：按控制组实例推送（objectId=item.getObjectId()），范围值 `"40,50"` 拆分为两个参数，空值推空串
- `saveSupplyRH()`：项目级推送（objectId=项目ID），本次 `saveVentDomeConf()` 与其 objectId 语义一致

目标推送结构（与 PRD curl 示例逐字对齐）：

```json
[
  {
    "objectId": "Pj0000001",
    "paramCode": "control_vent_dome_exhaust_open_temp",
    "paramValue": "45",
    "paramLabel": "开启排风机的穹顶温度设定值Ts"
  }
]
```

## Goals / Non-Goals

**Goals:**
- 将 `group_control_vent_dome` 下全部 22 个参数按项目级推送至 DTP
- objectId 统一使用当前项目ID
- 未配置参数推送 defaultValues，确保 DTP 拿到可用值
- 复用现有 `queryShellConfigByScene` + `saveList` 链路，零新增接口

**Non-Goals:**
- 不做增量判断（Redis 缓存比对），采用全量推送
- 不对已保存配置做任何数据迁移
- 不修改 `saveFreqRangeConf` 等既有同步方法的逻辑
- 不新增 Feign 接口、不修改 DTP 侧服务
- 不修改壳配置查询/保存链路（`ShellConfigServiceImpl` 零改动）

## Decisions

### 1. objectId 取项目ID

**选择**: `objectId = ContextUtil.getProjectId()`（调度方法开头已 set）

**理由**: 穹顶通风分组为项目级配置（无对象实例），PRD curl 示例中的 "Pj0000001" 即项目ID；与 `saveSupplyRH()` 的项目级推送先例一致。

**备选方案**: 复用 `item.getObjectId()`——项目级分支该字段为空，取不到值，排除。

### 2. 空值取默认值（区别于 freq-range 的推空串）

**选择**: `paramValue = item.getShellConfigValue()`（value 空白时返回 defaultValues，兜底 `StrUtil.blankToDefault(..., "")`）

**理由**:
- 用户决策：空则取默认值；PRD curl 示例中 "45" 即 `exhaust_open_temp` 的 defaultValues
- 22 个参数均由枚举保证非空 defaultValues（含 judge_mode 的 "1"），DTP 恒收到有效值
- 与 freq-range 推空串的差异说明：freq-range 按控制组实例推送且 PRD 明确空串清空语义；本次项目级参数无"清空"语义，默认值即最终可用值

**备选方案**: 推空串（仿 freq-range）——DTP 侧需再兜底默认值，职责外移，排除。

### 3. 全量推送，不做增量判断

**选择**: 每次定时任务全量推送 22 条

**理由**: 参数数量固定、单项目单实例，数据量极小；与 `saveFreqRangeConf` / `saveFlowDemandConf` 现状一致，避免 Redis 比对维护成本。

### 4. paramCode 原样使用，不做拆分映射

**选择**: `paramCode = item.getCode()`、`paramLabel = item.getName()`

**理由**: 与 PRD curl 示例逐字对齐（`control_vent_dome_exhaust_open_temp` / "开启排风机的穹顶温度设定值Ts"）；与 freq-range 的"一拆二"不同，本需求为 1 参数 : 1 推送条目。

### 5. 遍历分组全部 item，不做 code 白名单

**选择**: 遍历 query 结果的全部 items，不按 code 过滤

**理由**: 分组内恰好为 22 个参数，满足"每个参数都推送"；未来分组内新增参数自动纳入推送（零代码改动）。

## Risks / Trade-offs

- **[默认值语义]** DTP 无法区分"用户配置了 45"与"未配置取默认 45" → 可接受，控制逻辑只需要有效值
- **[judge_mode 编码]** 推送 "1"/"2"/"3" 字符串，依赖 DTP 侧按编码约定识别 → 编码约定已在 `shell-config-vent-control` 固化，本期不含 DTP 侧改造
- **[全量推送频率]** 每 5 分钟 22 条 → 风险低，数据量小
- **[分组内新增参数]** 自动纳入推送（预期行为）；若未来新增"不推送"参数需改为白名单过滤 → 在扩展时评审
