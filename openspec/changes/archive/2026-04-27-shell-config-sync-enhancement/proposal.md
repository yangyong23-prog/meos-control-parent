## Why

当前壳配置（Shell Config）系统已支持将部分配置参数同步至BDTP孪生服务，但仅覆盖冷站（ACCC）的"不能同时开启的冷机"和空调冷源（ACAC）的"冷机开启限制规则"。

根据0501迭代PRD需求，需新增三个关键参数的BDTP同步：
1. 空调冷源（ACAC）和冷源（ACCC）的"冷机间隔开启限制规则"
2. 空调冷源（ACAC）和冷源（ACCC）的"屏蔽所有控制指令"
3. 空调冷热源（ACAI）的"主机开启台数限制规则"

这些参数是自动化控制逻辑的关键输入，需要同步至BDTP以支持孪生计算和边缘控制决策。

## What Changes

- 新增3个BDTP同步任务方法：`saveStartIntervals()`、`saveShieldControl()`、`saveHostUnitLimit()`
- 扩展 `DtpTwinClient` Feign接口，新增 `saveStartIntervals` 和 `reviseObjectInSys` 端点
- 在 `InnerSchdule` 定时任务中集成新同步逻辑，保持每5分钟轮询机制
- 支持空调冷源（ACAC）、冷源（ACCC）、空调冷热源（ACAI）三种系统的配置同步

## Capabilities

### New Capabilities
- `shell-config-sync-start-intervals`: 冷机间隔开启限制规则的BDTP同步，覆盖空调冷源（ACAC）和冷源（ACCC）系统，调用 `/twc/conf/saveStartIntervals` 接口
- `shell-config-sync-shield-control`: 屏蔽所有控制指令的BDTP同步，覆盖空调冷源（ACAC）和冷源（ACCC）系统，调用 `/twc/revise/reviseObjectInSys` 接口
- `shell-config-sync-host-unit-limit`: 主机开启台数限制规则的BDTP同步，覆盖空调冷热源（ACAI）系统，调用 `/twc/conf/saveExclusions` 接口

### Modified Capabilities
<!-- 无现有能力的需求变更，均为新增能力 -->

## Impact

- **新增文件**:
  - `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service/DtpTwinClientService.java`（扩展接口）
  - `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside/feign/DtpTwinClient.java`（扩展Feign客户端）
  - `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside/twin/DtpTwinClientServiceImpl.java`（边缘端实现）
  
- **修改文件**:
  - `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/task/scheduled/InnerSchdule.java`（新增3个同步方法）
  - `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/outside/DtpTwinClientServiceImpl.java`（saas端空实现，保持抛异常）

- **无影响**:
  - SaaS端壳配置保存逻辑（`ShellConfigServiceImpl.save()`）无需修改
  - 现有6个同步任务不受影响
  - 数据库表结构无变更
