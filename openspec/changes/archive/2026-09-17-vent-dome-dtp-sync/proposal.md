## Why

PRD-260902《穹顶通风控制逻辑（电动气窗+风机）》的 22 个穹顶通风壳配置参数（分组 `group_control_vent_dome`）已实现配置与保存（见已归档 change `2026-09-11-prd-260902-vent-control-condition-input`），但该期明确"本期不做 BDTP 推送"，参数尚未同步至 DTP 孪生服务。

DTP 侧通风控制逻辑（穹顶排风机/电动气窗控制）依赖这批阈值参数（穹顶温度设定值、室外空气状态判断温差与判定方式、恶劣天气判定阈值等）执行计算，缺少推送会导致控制侧无法获取项目最新配置。本次按 PRD 给定推送结构（`/twc/conf/saveList`、objectId=项目ID、每条 `{paramCode, paramValue, paramLabel}`）补齐同步链路。

## What Changes

- 新增 DTP 同步方法 `saveVentDomeConf()`，将 `group_control_vent_dome` 分组下**全部 22 个**壳配置参数同步至 DTP，调用 `/twc/conf/saveList` 接口
- 推送以**项目ID**为 objectId（穹顶通风为项目级配置，无对象实例，如 "Pj0000001"）
- 每条记录：`paramCode` = 壳配置 `item.getCode()`（如 `control_vent_dome_exhaust_open_temp`），`paramLabel` = `item.getName()`，不拆分、不重命名
- 值取值：已保存配置值优先，为空时取 `defaultValues`（`getShellConfigValue()` 语义）——未配置的 `control_vent_dome_exhaust_open_temp` 推 "45"，未配置的 judge_mode 推 "1"
- 全量推送（22 条/次），不做增量比对，与 `saveFreqRangeConf` 保持一致
- `saveVentDomeConf()` 挂载至 `InnerSchdule.synShellConfigToTwin()`（`saveFreqRangeConf()` 之后），调度周期沿用每 5 分钟

## Capabilities

### New Capabilities

- `vent-dome-dtp-sync`: 穹顶通风 22 个壳配置参数的项目级 DTP 同步——覆盖分组内全部参数项、空值取默认值、以项目ID为 objectId 推送至 `/twc/conf/saveList`

### Modified Capabilities

（无）

## Impact

- **common 模块**：零改动（复用已有 `ShellConfigItem.getShellConfigValue()`、`AbstractCommonServer.queryShellConfigByScene`）
- **compute-edge 模块**：`InnerSchdule.java` 新增 `saveVentDomeConf()` 方法 + 调度方法内追加一行调用
- **DTP 接口**：复用已有 `/twc/conf/saveList` 端点（`DtpTwinClientService.saveList`），无需新增接口
- **数据契约**：DTP 侧收到 22 条项目级参数；未配置项目收到默认值而非空值（"未配置"与"默认值"对 DTP 不可区分，可接受）
- **上游依赖**：`group_control_vent_dome` 分组与 22 个参数枚举已由 PRD-260902 落地，本 change 无枚举改动
