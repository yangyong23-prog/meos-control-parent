# PRD-260801 取消壳参数推送BDTP

## Summary

取消"冷站系统及设备控制"中两个"设备频率设定"壳配置参数向BDTP孪生服务的定时推送。

## Context

当前边缘端定时任务 `InnerSchdule.synShellConfigToTwin()` 每5分钟将多组壳配置参数同步至BDTP。其中 `saveClassConfs()` 方法按设备类（classCode）推送3组"设备频率设定"参数：

- `POLICY_DEVICE_FAN_FREQ` → ACOTC（冷却塔风机组）
- `POLICY_DEVICE_PUMP_FREQ` → ACCPC（冷冻水泵组）
- `POLICY_DEVICE_COLDHOT_FREQ` → ACPPC（冷热源泵组）

产品需求取消前两组（ACOTC、ACCPC）的推送，保留第三组（ACPPC）。

## Goals / Non-Goals

**Goals:**
- 取消 POLICY_DEVICE_FAN_FREQ（ACOTC）和 POLICY_DEVICE_PUMP_FREQ（ACCPC）的BDTP推送
- 保留 POLICY_DEVICE_COLDHOT_FREQ（ACPPC）的推送不受影响

**Non-Goals:**
- 不清理BDTP侧已存在的旧数据
- 不修改 `saveFreqRangeConf()` 等其他推送逻辑
- 不修改 Redis 缓存逻辑

## Affected Files

- `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/task/scheduled/InnerSchdule.java`
