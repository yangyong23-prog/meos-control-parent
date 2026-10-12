# 设计评审文档：云端持久化数据查询规则（PRD-260901）

## 一、背景与目标

产品在壳范围配置中新增"云端持久化数据显示规则"，支持两种查询逻辑：

| 查询逻辑            | 行为                                      | 备注   |
| --------------- | --------------------------------------- | ---- |
| 查询同一个时刻点的数据（默认） | 保持当前逻辑：查询距离【当前时间-5min】前最近一个 5min 分精度的数据 | 现状不变 |
| 查询距离当前最新的数据     | 查询（当前时间-间隔 ~ 当前时间）范围内最新上数的数据，不限制分精度     | 新增能力 |

新增配置项"距离当前的时间间隔"（默认 1h，可设 1-168h），仅在选择"查询距离当前最新的数据"时出现。

## 二、改什么

### 2.1 生效规则（核心）

| 前端请求头标识                         | 壳配置查询逻辑         | 查询行为                                   |
| ------------------------------- | --------------- | -------------------------------------- |
| 无标识                             | 任意              | 按现有逻辑执行（与现状完全一致）                       |
| 有标识（`removeDefaultIpType=true`） | 查询同一个时刻点的数据（默认） | 按现有逻辑执行（与现状完全一致）                       |
| 有标识（`removeDefaultIpType=true`） | 查询距离当前最新的数据     | 使用默认分精度的查询：时间范围=（当前-间隔 ~ 当前），**不传分精度** |

**关键原则：本次改造只解决"默认值"的问题。显式指定了分精度类型的查询（如 DAY、1d、5min 等），是产品功能需求，保持指定的分精度不变。**

只有第三行组合会改变行为，其余组合与改造前无任何差异。接口入参、响应格式均不变。

### 2.2 改动方式概述

- **后端**：仅在底层 2 个查询方法（实时值查询、最近一小时查询）中增加判断逻辑，其余约 136 处调用点无需改动；仅"使用默认分精度"的查询受影响，显式指定分精度的查询保持不变
- **壳配置**：新增分组"云端持久化数据显示规则"及 2 个配置项，复用现有壳配置保存/读取体系，按项目生效
- **SSE 实时推送**：前端行为不变，仍在订阅请求头加标识；后端在订阅建立时检测请求头，有标识则转存到订阅参数，推送查询时按"订阅参数标识 + 壳配置"判断精度
- **前端**：见"四、前端应该做什么"

## 三、影响范围

### 3.1 业务影响范围（产品限定）

冷源、冷热源、热源、空调冷源、空调热源、空调冷热源（**空调系列暂不改动**）。

| 页面                   | 行为变化（仅最新数据模式）         |
| -------------------- | --------------------- |
| 系统图、空间三维模型           | 不再显示"更新时间"            |
| 关系图、设备详情、设备列表、空间三维模型 | 按（当前-间隔 ~ 当前）查询，不传分精度 |
| 时间范围内无新数据            | 页面显示 --               |
| 不同信息点最新时间点           | 可能不一致（产品已定义的语义）       |

### 3.2 影响范围收窄说明

- **行为变化的接口**：仅前端加了标识 + 壳配置"最新数据"的接口
- **未加标识的接口**：完全不变
- **回归范围**：仅冷热源模块 + 前端标识过的接口

### 3.3 涉及的接口（前端已梳理，2026-08-26）

以下为前端梳理的需要加请求头标识的接口清单。后端已逐个核对调用链，标记如下：

- **✓ 涉及**：接口调用链确实走到云端持久化 IOT 信息点值查询，需加请求头标识 `removeDefaultIpType=true`
- **✗ 不涉及**：接口只查对象/配置/事件/模板等数据，不查询信息点值，无需加标识

**改造接口范围**：冷源、冷热源、热源涉及【关系图/模型图、设备详情、设备列表、控制台】；空调冷源、空调热源、空调冷热源涉及【关系图/模型图、设备详情、设备列表】。

#### 空调冷源 / 空调热源 / 空调冷热源

**系统图 header**

- ✓ 涉及：无
- ✗ 不涉及：
  - `strategyIntervention/getInfoCodeDataTimeAndPrecisionType`（纯计算返回分精度配置，无 IOT 查询）

**关系图、空间三维模型**

- ✓ 涉及：
  - `system/graph/instance/{id}`（系统图实例查询，getInfoCodeValues 查设备节点信息点值）
  - `systemGraphEditor/getSystemGraphDetailById`（listInfoCodeValueInOneHour）
  - `instance/listObjectsByTackValueLogic`（带信息点值过滤条件时查 IOT，listInfoCodeValueInOneHour）
  - `airconditioning/listEquipmentsToModuleView/SYSTEM_RELATED_EQUIPMENT`（listInfoCodeValueInOneHour）
  - `airconditioning/listEquipmentsToModuleView/EQUIPMENT_IN_SYSTEM`（listInfoCodeValueInOneHour）
  - `airconditioning/listEquipmentsToModuleView/EQUIPMENT_IN_SPACE`（listInfoCodeValueInOneHour）
  - `infoCodeConfig/listInfoCodeConfigsByObjectId`（信息点配置带 IOT 值：fillInfoCodeDataModel 策略查 IOT，带动态过滤时另走 filterDynamicInfoCode）
  - `running-monitor/batchQueryInfoCode`（listInfoCodeValue）
  - `server-event/sse/subscribe`（订阅后推送信息点值）
- ✗ 不涉及：
  - `system/graph/menu/config`（菜单配置）
  - `system/graph/instance/select`（只查系统图表定义，不查信息点值）
  - `system/graph/icon/filter/config`（样式配置）
  - `system/graph/pipeline/style/config`（样式配置）
  - `system/graph/multiple/config`（样式配置）
  - `system/graph/area/style/config`（样式配置）
  - `knowledge/listInfoCodes`（信息点编码定义，非信息点值）
  - `shellConfig/query`（壳配置值查询）
  - `abnormalEvent/listAbnormalEvent`（异常事件库）
  - `running-monitor/queryAbnormalEventList`（异常事件库）
  - `server-event/sse/subscribe?ABNORMAL_EVENT`（订阅推送异常事件，不查信息点值）

**系统图控制台**

- ✓ 涉及：
  - `weather/queryWeather`（气象站数据，queryObjectIdPersistentDataList）
  - `equipment/infoCodeConfig/listInfoCodeConfigsByObject`（信息点配置带 IOT 值，listInfoCodeValue）
  - `server-event/sse/subscribe`（订阅后推送信息点值）
- ✗ 不涉及：
  - `dtp-meos-server/demandStrategy/getChiller`（控制服务 dtp-meos-server，由控制服务自行改造）
  - `dtp-meos-server/demandStrategy/listScheduleInfoCodeDefine`（控制服务）
  - `dtp-meos-server/runConsole/allControllerParameter`（控制服务）
  - `dtp-meos-server/runConsole/airTerminalZoneControllerParameter`（控制服务）
  - `instance/listSpacesByRunningMonitor/AIR_CONDITIONING_COLD_SOURCE`（空间列表查询）
  - `running-monitor/listAccRuntimeClass`（泵组 + 壳配置查询）

**对象详情弹窗**

- ✓ 涉及：
  - `infoCodeConfig/listInfoCodeConfigsByObjectId?DETAIL`（同 listInfoCodeConfigsByObjectId）
  - `equipment/infoCodeConfig/listInfoCodeConfigsByObject`（同 listInfoCodeConfigsByObject）
  - `server-event/sse/subscribe?INFO`（订阅后推送信息点值）
- ✗ 不涉及：
  - `airconditioning/getForceControlStatus`（读壳配置）

**设备列表（仅空调冷源）**

- ✓ 涉及：
  - `infoCodeConfig/listInfoCodeConfigsByObjectId?WEB_CONFIG`（同 listInfoCodeConfigsByObjectId）
  - `instance/listObjectsByTackValueLogic`（按信息点值过滤对象）
  - `infoCodeConfig/listInfoCodeConfigsByTemplateId`（带动态信息点过滤参数时 filterDynamicInfoCode 查 IOT 值，listInfoCodeValueInOneHour）
  - `server-event/sse/subscribe?IOT_DATA_CHANGE`（订阅后推送信息点值）
- ✗ 不涉及：
  - `infoCodeConfig/listTemplateTypes`（模版类型列表）
  - `infoCodeConfig/treeTemplateType`（模版类型树）
  - `instanceConfig/queryConfigEquipmentListHeader`（表头配置）

#### 冷源 / 冷热源 / 热源

**关系图/模型图 IOT 数据**

- ✓ 涉及：
  - `running-monitor/querySystemRunningParam`（运行参数 queryIotRealData + 效率指标 queryIpDataOnMoment）
  - `system/graph/instance/{id}`（系统图实例查询，getInfoCodeValues 查设备节点信息点值）
  - `systemGraphEditor/getSystemGraphDetailById`（listInfoCodeValueInOneHour）
  - `running-monitor/queryInfoCodeConfigByObjectIds`（信息点配置带 IOT 值）
  - `running-monitor/batchQueryInfoCodeConfig`（信息点配置带 IOT 值）
- ✗ 不涉及：
  - `running-monitor/queryObjectGroupInfoBySystemId`（对象/空间/管网关系数据）
  - `system/graph/menu/config`（菜单配置）
  - `system/graph/instance/select`（只查系统图表定义）
  - `strategyIntervention/getNumberOfChillers`（冷机分组对象查询）
  - `abnormalEvent/listAbnormalEvent`（异常事件库）
  - `shellConfig/queryByInfoCode`（壳配置值查询）

**控制面板**

- ✓ 涉及：
  - `strategyIntervention/chillerStrategy/listRunningStrategy`（运行状态 queryObjectIdPersistentDataList，显式 5min 分精度）
  - `strategyIntervention/lastFreezePumpGroup/listRunningStrategy`（同上）
  - `hotColdStationStrategyIntervention/listRunningStrategy`（冷热源，同上）
  - `weather/queryWeather`（气象站数据，queryObjectIdPersistentDataList）
- ✗ 不涉及：
  - `cold-heat/queryPumpGroupObjectByPumpId`（冷热源，对象关系查询）
  - `dtp-meos-server/runConsole/airTerminalZoneControllerParameter`（控制服务）

**对象弹窗**

- ✓ 涉及：
  - `running-monitor/queryInfoCodeConfigByObjectIds`（信息点配置带 IOT 值）
  - `equipment/infoCodeConfig/listInfoCodeConfigsByObject`（热源，同 listInfoCodeConfigsByObject）
  - `airTerminal/getObjectTag`（mapEquipmentIdAndTag → queryIotRealDataListByList 查 IOT 实际状态）
- ✗ 不涉及：
  - `running-monitor/querySingleAbnormalEventList`（异常事件库）

> 注：生活给水、气源系统、光伏系统的接口结构与此类似，但不在本次改造范围（产品影响范围未包含）。控制台相关接口涉及控制服务（dtp-meos-server），由控制服务自行改造。
> 
> 核对汇总：前端梳理接口去重后共 44 个，其中 ✓ 涉及 17 个（需加请求头标识），✗ 不涉及 27 个（含 5 个控制服务 dtp-meos-server 接口、22 个对象/配置/事件/模版类查询接口）。

### 3.4 不受影响的非接口链路（已确认）

- **空间分析定时任务** ×2（xxl-job）：查询空间 CO2/温度，与冷热源无关，保持现状
- **运行分析报表定时生成**（xxl-job）：不经过 IOT 实时查询，不受影响
- **值班冷源卡片**：显式按天精度（DAY）查询，属产品功能需求，保持现状
- **控制台查询**（冷源/热源/冷热源）：前端直接调用控制服务接口，不在本次后端改造范围，由控制服务自行改造
- **定时任务、边缘端、历史数据/报表查询**：均不受影响

---

## 四、前端应该做什么

1. **壳配置页新增分组**："云端持久化数据显示规则"，两个配置项：持久化数据查询逻辑（默认"查询同一个时刻点的数据"）、距离当前的时间间隔（默认 1h，可设 1-168h，仅选择"查询距离当前最新的数据"时出现）
2. **接口请求头加标识**：在 3.3 列出的接口请求头加 `removeDefaultIpType=true`（值必须为 true）；SSE 订阅请求同样加请求头即可，后端自动转存到订阅参数
3. **查询请求调整**：壳配置=最新数据时，加标识接口的请求 header 不传 ipType，时间范围传（当前-间隔 ~ 当前）；壳配置=同一时刻点（默认）时按现状传
4. **展示配合**：壳配置=最新数据时，系统图页面、空间三维模型不再显示"更新时间"

---

## 五、测试要点

### 5.1 功能测试

| 测试场景                                       | 预期结果                        |
| ------------------------------------------ | --------------------------- |
| 请求头无标识，任意壳配置                               | 与改造前一致（同一时刻点 5min 精度）       |
| 请求头标识值非 true                               | 视为无标识，与改造前一致                |
| 请求头 `=true`，壳配置=同一时刻点（默认）                  | 与改造前一致                      |
| 请求头 `=true`，壳配置=最新数据，间隔=1h                 | 时间范围=（当前-1h ~ 当前），不传分精度     |
| 请求头 `=true`，壳配置=最新数据，间隔=168h               | 时间范围=（当前-168h ~ 当前），不传分精度   |
| 请求头 `=true`，壳配置=最新数据，但查询显式指定了分精度（如 DAY、1d） | **保持指定的分精度不变**（显式指定是产品功能需求） |
| 最新数据模式下时间范围内无新数据                           | 页面显示 --                     |
| 不同信息点最新时间点不同                               | 按产品定义语义展示                   |

### 5.2 兼容性测试

| 场景        | 预期                                     |
| --------- | -------------------------------------- |
| 边缘端       | 不受影响，设备状态正常                            |
| 历史数据/报表查询 | 不受影响                                   |
| 定时任务      | 不受影响，正常执行                              |
| SSE 推送    | 订阅请求头有标识 + 壳配置最新数据时，推送查询不传分精度；否则与改造前一致 |

---

## 六、待确认项

1. 壳配置分组/配置项的 code 命名（需与前端配置页对齐）