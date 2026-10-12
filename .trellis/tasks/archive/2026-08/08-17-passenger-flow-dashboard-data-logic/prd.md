# 【PRD-260802】客流dashboard数据取值逻辑

## Goal

客流上线后支持独立于门组交付：项目维度即使无门组也可正常展示客流数据；门组维度仅展示有 infoCode 实例化规则的门组。Dashboard 出入口统计、楼层地图门组展示、值班首页客流卡片均需按实例化规则过滤。

## 背景

- 客流 system 可交付门组也可不交付。门组不交付时，项目总客流数据需正常显示。
- 现有 `getPassagewayHeadCount()` 直接拉取所有 CFDRG 门组 + 项目，未按 infoCode 实例化规则过滤。
- 楼层地图中门组展示也未按规则过滤。
- 值班首页客流卡片当前仅取项目级 `accmInPeopleFlow`，未考虑门组依赖。

## Requirements

### R1: Dashboard 出入口人数统计过滤（5.1）

**接口**: `POST /passengerFlow/getPassagewayHeadCount`

**过滤规则**:
- **项目统计**: 若项目对象的 infoCode `accmInPeopleFlow`（累计进入人次）或 `accmOutPeopleFlow`（累计离开人次）**任意一个**有实例化规则，则展示项目统计行。
- **门组统计**: 若门组(CFDRG)的 infoCode `accmInPeopleFlow` 或 `accmOutPeopleFlow` **任意一个**有实例化规则，则展示该门组统计行。

**规则判断逻辑**: 使用 `mapObjectIdAndExistsRuleInfoCodes` 查询对象是否存在指定 infoCode 的实例化规则，取**并集**（任意一个命中即保留），而非交集。

**约束**:
- 查询门组信息点数据前先过滤，减少无效 IOT 查询。
- 项目统计行的 objectId 为 projectId，名称为国际化 key `key_c8ff811cb5234f69b587f5dd1fa73141`（"项目"）。

### R2: 楼层地图门组过滤（5.2）

**范围**: 楼层地图中展示的门组列表。

**过滤规则**: 仅展示门组(CFDRG)的 infoCode `accmInPeopleFlow` 或 `accmOutPeopleFlow` **任意一个**有实例化规则的门组对象。

**实现要点**: 经研究确认，CFDRG 门组在整个 common 模块中仅通过 `getGateGroupList()` 查询，楼层地图与 Dashboard 共用 `getPassagewayHeadCount()` 接口。在 `getPassagewayHeadCount()` 中增加规则过滤即可同时覆盖 Dashboard（5.1）和楼层地图（5.2）。

### R3: 值班首页影响确认（5.3）

**接口**: `POST /duty/homePage`（PASSENGER_FLOW 卡片）

**结论**: 无需调整。`PassengerFlowHomePageCardDataHandler.execute()` 仅取项目级 `accmInPeopleFlow` 数据，不依赖门组信息，当前逻辑已满足需求。

### R4: infoCode 实例化规则查询

**涉及的 infoCode**:
| infoCode | 含义 | 来源 |
|---|---|---|
| `accmInPeopleFlow` | 累计进入人次 | `InfoCodeEnum.ACCM_IN_PEOPLE_FLOW` |
| `accmOutPeopleFlow` | 累计离开人次 | `InfoCodeEnum.ACCM_OUT_PEOPLE_FLOW` |

**查询方式**: 调用 `AbstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds, infoCodes)`：
- 云端：通过 `DtpRwdClientService.mapObjectIdAndInstantiateInfoCodes` 查询持久化规则
- 边缘端：通过 `DtpRwdClientService.listTwinObjectRuleById` 查询孪生规则

## Acceptance Criteria

- [ ] Dashboard 出入口统计：项目仅在其 `accmInPeopleFlow` 或 `accmOutPeopleFlow` 有实例化规则时展示
- [ ] Dashboard 出入口统计：门组(CFDRG)仅在其 `accmInPeopleFlow` 或 `accmOutPeopleFlow` 有实例化规则时展示
- [ ] 门组不交付时（无实例化规则），项目维度仍可独立展示客流数据
- [ ] 楼层地图：仅展示 `accmInPeopleFlow` 或 `accmOutPeopleFlow` 有实例化规则的门组
- [ ] 值班首页：确认客流卡片取值逻辑，与产品对齐后决定是否调整
- [ ] 边缘端和云端均正确过滤（规则查询路径不同）
- [ ] 项目统计行始终在返回列表首位（保持现有 `LinkedHashMap` 插入顺序）

## 涉及模块

| 模块 | 文件 | 改动类型 |
|------|------|----------|
| Dashboard + 楼层地图 | `PassengerFlowServiceImpl.getPassagewayHeadCount()` | 新增规则过滤（一处改动覆盖两需求） |
| 值班首页 | `PassengerFlowHomePageCardDataHandler.execute()` | 无需改动（已确认） |

## Notes

- 规则判断为**并集**逻辑（任意一个 infoCode 有规则即保留），与现有的 `filterInstantiateRuleToIntersection`（交集）不同。
- 项目对象无 classCode，`mapObjectIdAndExistsRuleInfoCodes` 的云端/边缘端实现均不依赖 classCode 做查询，可使用任意占位 key。
- 详细代码分析见 `research/code-analysis.md`。