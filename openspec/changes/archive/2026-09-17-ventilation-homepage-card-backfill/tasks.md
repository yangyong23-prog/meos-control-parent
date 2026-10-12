## 1. 卡片补齐实现

- [x] 1.1 在 `VentilationHomePageCardDataHandler.execute()` 中 `buildChildCardDateVO` 调用之前新增补齐逻辑：将 `dutyHomePageGroup.getSubStat()` 复制到 `LinkedHashMap`（null/空时以空 Map 起步），遍历 `queryMap`，对缺失 key 补 `new DutyHomePageDTO()` 且 `setTotal(v.getObjectIds().size())`
- [x] 1.2 新增 `DutyHomePageDTO` import，补齐逻辑附中文注释（说明 DTP 对无统计数据分组不返回 key 的背景与「其余统计保持 0」的语义）

## 2. 验证

- [x] 2.1 执行 Maven 编译确认无编译错误（`mvn compile -pl common -am -DskipTests`）
- [ ] 2.2 接口手工验证：DTP 部分返回场景缺失分组卡片齐全、`equipmentTotal`=入参设备数、其余统计为 0；DTP 返回空 Map 场景 N 张卡全出；DTP 全量返回场景与现状一致；其它运行监测类型无回归
