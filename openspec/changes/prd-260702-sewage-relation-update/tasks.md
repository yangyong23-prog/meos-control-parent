## 1. info code 框架 fallback 能力

- [x] 1.1 在 `InfoCodeTakeValueLogicRwdObjectDetailModel` 中新增 `List<DtpRwdObjectQueryParam> fallbackQueryParams` 字段
- [x] 1.2 在 `InfoCodeRwdObjectDetailByTemplateContext.fillObjects()` 中实现整批 fallback 逻辑：主查询非空则使用主查询结果，否则依次尝试 `fallbackQueryParams`
- [x] 1.3 在 `InfoCodeConfigImportContext` 中增加对 `fallbackQueryParams` 的合法性校验
- [x] 1.4 编译并运行相关单元测试，确保未配置 fallback 的现有逻辑行为不变
- [ ] 1.5 验证 `/equipment/infoCodeConfig/listInfoCodeConfigsByObject` 返回的 RWD_OBJECT_DETAIL 关系信息点（如 WSSTOR/WSSTLU 详情的 `submersibleSewagePump`）已应用整批 fallback

## 2. Java 侧污水设备关系 fallback 方法

- [x] 2.1 在 `DtpRwdClientService` 中新增 `listSubmersibleSewagePumpByEquipmentIds` 接口方法
- [x] 2.2 在 `DtpRwdClientServiceImpl` 中实现该方法：先查 `MechSubset/Eq2Eq`，无结果再查 `MechLocation/Inside`
- [x] 2.3 将 `RunningMonitorSewageStrategy.updateRelatedPumps` 改为调用新的 fallback 方法
  - 覆盖 `/dashboardGridChart/queryEquipment` 返回的 `SewageEquipVO.relEquipment`
  - 覆盖运行监测卡片中的：关联潜污泵数量、运行状态统计、故障数量统计、故障潜污泵实例列表、液位详情中的关联潜污泵列表
- [x] 2.4 将 `WaterSupplyHomePageCardDataHandler` 中的泵查询改为调用新的 fallback 方法
  - 覆盖值班首页 WSSTOR/WSSTLU 卡片的：关联潜污泵数量、关联潜污泵运行数量

## 3. Excel 配置更新

- [x] 3.1 修改 `【SEWAGE-TABLE_LIST】设备列表模版.xlsx`：
  - WSSTOR 的 `relSubmersibleBlowdownPump` 主查询改为 `MechSubset/Eq2Eq` 并添加 `MechLocation/Inside` fallback
  - WSSTLU 的 `relSubmersibleBlowdownPump` 主查询改为 `MechSubset/Eq2Eq` 并添加 `MechLocation/Inside` fallback
  - WSSSTSP 的 `relOilRemoveDevice` 与 `relLiftingUnit` 主查询改为 `MechSubset/Eq2Eq` 并添加 `MechLocation/Inside` fallback
- [x] 3.2 修改 `【SEWAGE-DETAIL】设备详情页模版.xlsx`：
  - WSSTOR 的 `submersibleSewagePump` 主查询改为 `MechSubset/Eq2Eq` 并添加 `MechLocation/Inside` fallback
  - WSSTLU 的 `submersibleSewagePump` 主查询改为 `MechSubset/Eq2Eq` 并添加 `MechLocation/Inside` fallback

## 5. 关系方向修正

> 初始实现错误地沿用了旧关系的 `setRelationFrom + setObjTo` 模式，但新关系 `MechSubset/Eq2Eq` 方向与旧关系 `MechLocation/Inside` 恰好相反，需翻转 `relationFrom/relationTo` 与 `objFrom/objTo`。fallback 查询不变（旧关系方向本就正确）。详见 design.md Decision 5。

- [x] 5.1 修正 `DtpRwdClientServiceImpl.listSubmersibleSewagePumpByEquipmentIds` 新关系主查询方向：
  - `setRelationFrom` → `setRelationTo`
  - `.setObjTo(equipmentIds)` → `.setObjFrom(equipmentIds)`
  - 旧关系 fallback 查询保持 `setRelationFrom + setObjTo` 不变
- [x] 5.2 修正 `【SEWAGE-TABLE_LIST】设备列表模版.xlsx` 新关系主查询方向：
  - WSSTOR/WSSTLU `relSubmersibleBlowdownPump` 主查询：`relationFrom + objTo` → `relationTo + objFrom`
  - WSSSTSP `relOilRemoveDevice`/`relLiftingUnit` 主查询：`relationTo + objFrom` → `relationFrom + objTo`
  - 所有 fallback 查询保持不变
- [x] 5.3 修正 `【SEWAGE-DETAIL】设备详情页模版.xlsx` 新关系主查询方向：
  - WSSTOR/WSSTLU `submersibleSewagePump` 主查询：`relationFrom + objTo` → `relationTo + objFrom`
  - 所有 fallback 查询保持不变
- [x] 5.4 验证修正后 fallback 旧关系查询方向不受影响（仍为 `relationFrom + objTo`，语义正确）

## 6. 验证与上线

- [x] 4.1 执行 `mvn clean compile` 或项目等效构建命令，确认无编译错误
- [ ] 4.2 在本地/测试环境验证：新项目仅建立新关系时列表/详情/运行监测/值班首页/`/dashboardGridChart/queryEquipment`/`/equipment/infoCodeConfig/listInfoCodeConfigsByObject` 正常展示
- [ ] 4.3 在本地/测试环境验证：老项目仅建立旧关系时列表/详情/运行监测/值班首页/`/dashboardGridChart/queryEquipment`/`/equipment/infoCodeConfig/listInfoCodeConfigsByObject` 通过 fallback 正常展示
- [ ] 4.4 重新导入更新后的 Excel 配置到目标项目
- [ ] 4.5 与业务确认 `rc_object_relation_cfg` 是否需要新增 `MechSubset/Eq2Eq` 关系配置行
