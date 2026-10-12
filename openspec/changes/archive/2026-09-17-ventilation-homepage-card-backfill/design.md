## Context

通风值班首页 `VentilationHomePageCardDataHandler.execute()` 的查询入参 `queryMap` 由 `queryEquipGroupId()` 按项目实际设备动态分组构建（N 组，键为 `code:名称`）；聚合接口 `homepageFanEquipmentStatusStatistics` 对无统计数据的分组不返回 key，云端适配器 `HomePageCardIotDataCloud` 收尾时仅对已返回 key 覆写 total 并 `setSubStat`，导致缺统计分组的小卡片消失。返回空 Map（`{}`）时 `subStat` 为空但非 null，绕过 `buildNullData` 兜底，展示 0 张卡。

方案在探索阶段已收敛：只在 handler 填充处补齐，不改通用逻辑。

## Goals / Non-Goals

**Goals:**
- 通风小卡片集合恒等于入参动态分组集合（N 组，随项目设备构成变化）
- 缺统计分组卡片：`equipmentTotal` = 该组入参设备数量，其余统计字段为 0
- 云/边两种部署同时满足，且通用逻辑与其它运行监测类型零影响

**Non-Goals:**
- 不修改适配器契约（`HomePageCardIotDataCloud` / `HomePageCardIotDataEdge`）
- 不为照明/空调末端/厨房引入同样兜底（同类隐患存在，需产品另行确认）
- 不调整小卡片顺序/排序
- 不改动 `canJump` 及大卡片（cardTag）逻辑

## Decisions

### D1: 补齐落点在 handler 填充前

**决策**: 在 `VentilationHomePageCardDataHandler.execute()` 中、`buildChildCardDateVO` 调用之前，对 `subStat` 按 `queryMap` 补齐。

**理由**: 需求方明确「不改变通用逻辑」；该处一处改动即覆盖云/边两种部署（handler 位于 common 模块），爆炸半径最小。

**替代方案**: 适配器层通用补齐（会连带改变照明/末端/厨房展示行为，范围过大）；`buildChildCardDateVO` 内补齐（该方法拿不到 queryMap，不可行）。

### D2: 基线 = queryMap，动态 N

**决策**: 遍历 `queryMap` 找缺失 key；不硬编码组数、不枚举 `VentilationEquipStatisticsEnum.values()`。

**理由**: `queryMap` 由 `queryEquipGroupId()` 动态生成（无设备不出组），即「该项目该有的组」；枚举全量补齐会在无该类设备的项目凭空造卡；未来新增设备类型零维护。

**替代方案**: 枚举 values() 全量补齐 → 语义错误（会产生 0 设备分组）。

### D3: 补齐 DTO 显式设置 total

**决策**: 补齐的 `DutyHomePageDTO` 设置 `total = objectIds.size()`，其余字段依赖 int 默认 0。

**理由**: `buildChildCardDateVO` 会跳过 `total == 0` 的条目，不赋值等于没补；且与适配器「total=入参数量」的既有覆写规则一致，形成不变量：`equipmentTotal == 入参设备数`。

**替代方案**: 保持 0 交由下游过滤 → 卡片不出现，违背需求。

### D4: subStat 为 null 时以空 Map 起步

**决策**: 补齐前若 `subStat == null` 则以空 `LinkedHashMap` 起步。

**理由**: 云路径通常不会为 null（有 `buildNullData` 兜底），但边端 `HomePageCardIotDataEdge` 存在返回 `subStat = null` 的 rVo 的理论路径；一行判空防御，避免 NPE。

**替代方案**: 不判空 → 极端场景 NPE。

### D5: 保序追加

**决策**: 复制 DTP 返回结果到 `LinkedHashMap` 后追加缺失分组（DTP 返回的在前，补齐的在后）。

**理由**: 保持既有卡片顺序不变（DTP 响应顺序），补齐的卡追加在末尾；现状顺序本不保证，前端无强依赖，本次不引入排序。

**替代方案**: 按枚举顺序重排全部卡片 → 超出本次范围。

## Risks / Trade-offs

**[Risk] 空 Map 场景行为变化**（0 张卡 → N 张卡）→ 正是需求要求（「就算没有返回也要展示」）。

**[Risk] 补齐卡片 5 项统计中 4 项为 0** → 0 值语义正确（无统计数据设备的运行/干预计数本就为 0）；前端按总数+运行数渲染即可。

**[Trade-off] 未同步修复照明/末端/厨房同类隐患** → 本次范围外，如产品确认可另开变更。

**[Trade-off] DTP 响应中若出现 queryMap 之外的 key** → 上游适配器收口时已对未知 key 抛 NPE（`queryParam.get(k)`），当前不可达；本次不改动该行为，故按 queryMap 复制不丢失任何有效数据。
