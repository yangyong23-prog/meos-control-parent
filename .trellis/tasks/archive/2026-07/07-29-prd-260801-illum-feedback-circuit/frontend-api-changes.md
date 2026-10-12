# 【PRD-26.0801】照度反馈条件按回路维度配置 — 接口变更说明

> 本文档面向前端开发，说明照度反馈调节条件从空间维度改为回路维度后，相关接口的请求/响应结构变更。

---

## 一、变更概述

| 变更点 | 说明 |
|---|---|
| 反馈条件下沉 | 自动开启/关闭条件从**空间级统一配置**改为**回路级逐条配置** |
| 新增回路字段 | 回路配置新增 `feedbackAdjustSetting` 对象，携带 4 个反馈条件字段 |
| 废弃空间级条件 | 功能类型级的 `spaceFeedbackAdjustSettings` 改为 `spaceFeedbackAdjustType`（仅枚举） |
| 新增校验接口 | `POST /check/feedbackConfig` 返回多空间冲突详情 |
| 保存接口校验 | 保存时仍做校验，但不返回详情，仅返回错误提示 |

---

## 二、涉及接口

| 接口 | 路径 | 变更类型 |
|---|---|---|
| 保存场景配置 | `POST /editSceneObjectSettings` | 请求体变更 |
| 查询场景配置 | `POST /getSceneObjectSettings` | 响应体变更 |
| **新增** 一致性校验 | `POST /check/feedbackConfig` | 新接口 |

---

## 三、保存接口 `POST /editSceneObjectSettings`

### 3.1 回路配置 `circuitSettings[]` 新增 `feedbackAdjustSetting` 对象

当回路 `settingMode = "FEEDBACK_ADJUSTMENT"` 时，携带 `feedbackAdjustSetting` 对象：

| 字段名 | 类型 | 说明 | 取值范围 |
|---|---|---|---|
| `feedbackAdjustSetting` | object | 反馈调节条件配置，`settingMode != FEEDBACK_ADJUSTMENT` 时为 `null` | — |
| `feedbackAdjustSetting.underOutdoorIlluminance` | number | 自动开启条件·照度阈值（lx） | 0-10000 正整数 |
| `feedbackAdjustSetting.underDuration` | integer | 自动开启条件·持续时间（min） | 0-300 整数，0=达到阈值即生效 |
| `feedbackAdjustSetting.overtopOutdoorIlluminance` | number | 自动关闭条件·照度阈值（lx） | 0-10000 正整数 |
| `feedbackAdjustSetting.overtopDuration` | integer | 自动关闭条件·持续时间（min） | 0-300 整数，0=达到阈值即生效 |

> 当 `settingMode = "OPEN"` 或 `"CLOSE"` 时，`feedbackAdjustSetting` 传 `null` 或不传。

### 3.2 `spaceFeedbackAdjustSettings` 变更为 `spaceFeedbackAdjustType`

功能类型级不再携带条件值对象，改为仅传反馈调节类型枚举：

```json
// ❌ Before（现状）：空间级统一携带条件
{
  "funcType": "1",
  "settingMode": "FEEDBACK_ADJUSTMENT",
  "spaceFeedbackAdjustSettings": {
    "underOutdoorIlluminance": 300,
    "underDuration": 10,
    "overtopOutdoorIlluminance": 500,
    "overtopDuration": 15
  }
}

// ✅ After（变更后）：空间级仅传类型枚举，条件下沉到回路级
{
  "funcType": "1",
  "settingMode": "FEEDBACK_ADJUSTMENT",
  "spaceFeedbackAdjustType": "OUTDOOR_ILLUMINANCE",
  "circuitSettings": [
    {
      "circuitId": "LTLTCC_aaa",
      "settingMode": "FEEDBACK_ADJUSTMENT",
      "feedbackAdjustSetting": {
        "underOutdoorIlluminance": 300,
        "underDuration": 10,
        "overtopOutdoorIlluminance": 500,
        "overtopDuration": 15
      }
    }
  ]
}
```

`spaceFeedbackAdjustType` 枚举值：

| 值 | 说明 |
|---|---|
| `INDOOR_ILLUMINANCE` | 室内照度调节 |
| `OUTDOOR_ILLUMINANCE` | 室外照度调节 |

#### 完整请求示例

```json
{
  "projectId": "Pj1234567890",
  "templateId": 1001,
  "sceneId": 2001,
  "instanceId": "Sp111",
  "instanceType": "SPACE",
  "globalSettings": [
    {
      "funcType": "1",
      "settingMode": "FEEDBACK_ADJUSTMENT",
      "spaceFeedbackAdjustType": "OUTDOOR_ILLUMINANCE",
      "circuitSettings": [
        {
          "circuitId": "LTLTCC_aaa",
          "lampFuncType": "1",
          "settingMode": "FEEDBACK_ADJUSTMENT",
          "feedbackAdjustSetting": {
            "underOutdoorIlluminance": 300,
            "underDuration": 10,
            "overtopOutdoorIlluminance": 500,
            "overtopDuration": 15
          }
        },
        {
          "circuitId": "LTLTCC_bbb",
          "lampFuncType": "1",
          "settingMode": "OPEN",
          "feedbackAdjustSetting": null
        }
      ]
    }
  ],
  "spaceSettings": []
}
```

#### 校验规则

| 规则 | 说明 |
|---|---|
| 最少配置一项 | `settingMode = "FEEDBACK_ADJUSTMENT"` 时，`feedbackAdjustSetting` 中自动开启和自动关闭条件**最少配置一项** |
| 阈值大小关系 | 两个条件都配置时，`underOutdoorIlluminance` 不能大于 `overtopOutdoorIlluminance`（允许相等） |
| 成对校验 | 自动开启条件：`underOutdoorIlluminance` 和 `underDuration` 必须同时有值或同时为空 |
| 成对校验 | 自动关闭条件：`overtopOutdoorIlluminance` 和 `overtopDuration` 必须同时有值或同时为空 |
| 多空间一致性 | 同一回路在多个空间均配置为反馈调节时，`feedbackAdjustSetting` 必须一致，否则返回错误提示 |

> 保存接口的校验仅返回简单错误提示，如需查看冲突详情请调用校验接口。

---

## 四、新增校验接口 `POST /check/feedbackConfig`

前端在保存前调用此接口，检查当前配置与同场景下其他空间的反馈调节条件是否冲突，返回详细冲突信息。

### 4.1 请求

请求体与 `POST /editSceneObjectSettings` 完全一致。

### 4.2 响应

无冲突时返回空列表：

```json
{
  "code": "success",
  "data": []
}
```

有冲突时返回冲突详情：

```json
{
  "code": "success",
  "data": [
    {
      "circuitId": "LTLTCC_aaa",
      "circuitName": null,
      "spaceConfigs": [
        {
          "instanceId": "Sp111",
          "instanceName": null,
          "feedbackAdjustSetting": {
            "underOutdoorIlluminance": 300,
            "underDuration": 10,
            "overtopOutdoorIlluminance": 500,
            "overtopDuration": 15
          }
        },
        {
          "instanceId": "Sp222",
          "instanceName": null,
          "feedbackAdjustSetting": {
            "underOutdoorIlluminance": 300,
            "underDuration": 20,
            "overtopOutdoorIlluminance": 500,
            "overtopDuration": 15
          }
        }
      ]
    }
  ]
}
```

### 4.3 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `circuitId` | string | 冲突回路ID |
| `circuitName` | string | 冲突回路名称（预留） |
| `spaceConfigs` | array | 各空间的配置详情 |
| `spaceConfigs[].instanceId` | string | 空间实例ID |
| `spaceConfigs[].instanceName` | string | 空间名称（预留） |
| `spaceConfigs[].feedbackAdjustSetting` | object | 该空间的反馈调节条件配置 |

---

## 五、查询接口 `POST /getSceneObjectSettings`

### 5.1 回路配置 `circuitSettings[]` 新增 `feedbackAdjustSetting` 对象

```json
{
  "circuitId": "LTLTCC_aaa",
  "circuitLocalName": "1F照明回路A",
  "funcType": "1",
  "lampFuncType": "1",
  "settingMode": "FEEDBACK_ADJUSTMENT",
  "feedbackAdjustSetting": {
    "underOutdoorIlluminance": 300,
    "underDuration": 10,
    "overtopOutdoorIlluminance": 500,
    "overtopDuration": 15
  }
}
```

### 5.2 废弃字段

| 废弃字段 | 替代字段 | 说明 |
|---|---|---|
| `feedbackAdjustmentConfig` | `feedbackAdjustSetting` | 回路级条件对象改名 |
| `spaceFeedbackAdjustSettings`（条件对象） | `spaceFeedbackAdjustType`（枚举） | 空间级改为仅传类型枚举 |

---

## 六、字段变更汇总

### 6.1 新增字段

| 层级 | 字段 | 类型 | 说明 |
|---|---|---|---|
| 回路级 | `feedbackAdjustSetting` | object | 反馈调节条件配置对象 |
| 回路级 | `feedbackAdjustSetting.underOutdoorIlluminance` | number | 自动开启照度阈值（lx） |
| 回路级 | `feedbackAdjustSetting.underDuration` | integer | 自动开启持续时间（min） |
| 回路级 | `feedbackAdjustSetting.overtopOutdoorIlluminance` | number | 自动关闭照度阈值（lx） |
| 回路级 | `feedbackAdjustSetting.overtopDuration` | integer | 自动关闭持续时间（min） |
| 功能类型级 | `spaceFeedbackAdjustType` | enum | 反馈调节类型（INDOOR_ILLUMINANCE/OUTDOOR_ILLUMINANCE） |

### 6.2 废弃字段

| 层级 | 字段 | 说明 |
|---|---|---|
| 功能类型级（保存） | `spaceFeedbackAdjustSettings` | 改为 `spaceFeedbackAdjustType` 枚举 |
| 回路级（查询） | `feedbackAdjustmentConfig` | 改为 `feedbackAdjustSetting` |
| 空间反馈调节（查询） | `spaceFeedbackAdjustSetting` | 已移除 |

### 6.3 不变字段

| 层级 | 字段 | 说明 |
|---|---|---|
| 功能类型级 | `funcType` | 照明回路功能类型 |
| 功能类型级 | `settingMode` | 类型配置模式 |
| 回路级 | `circuitId` | 照明回路ID |
| 回路级 | `settingMode` | 回路配置模式 |

---

## 七、`settingMode` 枚举值（不变）

| 值 | 说明 | 是否携带 `feedbackAdjustSetting` |
|---|---|---|
| `OPEN` | 开启 | 否，为 `null` |
| `CLOSE` | 关闭 | 否，为 `null` |
| `FEEDBACK_ADJUSTMENT` | 反馈调节 | 是，携带条件对象 |
| `NOT_CONFIGURED` | 未配置 | 否，为 `null` |
