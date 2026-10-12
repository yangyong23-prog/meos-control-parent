## 1. 枚举定义（common 模块）

- [x] 1.1 `ShellConfigTabEnum` 新增 `CONTROL_VENTILATION("control", "control_ventilation", "通风控制逻辑条件输入")`，置于 `CONTROL_SELECT` 之后、`POLICY_SITE` 之前
- [x] 1.2 `ShellConfigGroupEnum` 新增 `GROUP_CONTROL_VENT_DOME("group_control_vent_dome", "穹顶通风", "", false)`，置于 CONTROL 系列分组之后（systemType 为空串，保证走项目级分支）
- [x] 1.3 `ShellConfigItemEnum` 新增穹顶排风控制 4 参数（exhaust_open_temp/exhaust_open_duration/exhaust_close_temp/exhaust_close_duration），元数据按 design.md D5 表逐项核对
- [x] 1.4 `ShellConfigItemEnum` 新增电动气窗控制 1 参数（window_hold_duration）
- [x] 1.5 `ShellConfigItemEnum` 新增非营业时段室外空气状态判断 7 参数（off_judge_mode/off_wet_temp_diff/off_wet_low_duration/off_wet_high_duration/off_dry_temp_diff/off_dry_low_duration/off_dry_high_duration）
- [x] 1.6 `ShellConfigItemEnum` 新增营业时段室外空气状态判断 7 参数（on_* 系列，name 与 off 系列同名，code 仅 off/on 前缀差异）
- [x] 1.7 `ShellConfigItemEnum` 新增恶劣天气判断 3 参数（weather_rh_threshold/weather_rh_over_duration/weather_clear_duration）
- [x] 1.8 22 个参数的 desc 逐条填入 PRD「参数填写的tips说明」对应文本（PRD 内 ASCII 双引号已按 `\"` 转义，与 WEATHER_WEATHER_CHANGE 先例一致）；hint 按类型填"范围 （0-99）/（0-100）/（0-1440）"，单选类为空串；枚举项末尾三个 null 对应 valueType/valSource/editAuth（与现有 control 参数一致的构造参数个数与顺序）
- [x] 1.9 `ShellConfigItemExtendEnum` 新增 22 个条目注册 tabObjectName：穹顶排风控制×4、电动气窗控制×1、非营业时段室外空气状态判断×7、营业时段室外空气状态判断×7、恶劣天气判断×3

## 2. 验证与测试

- [x] 2.1 编译通过（`mvn compile -pl common`），无语法错误
- [ ] 2.2 调用 `/shellConfig/query`（scene=control、groups=["group_control_vent_dome"]）：返回 1 个分组、22 个 item，tabObjectName 覆盖 5 个分组名且数量为 4/1/7/7/3
- [x] 2.3 核对单选参数（off/on judge_mode）：step=-1、min/max 为空、defaultValues="干球温度判断"、无 valSource（已静态核对枚举定义 L362/L371，接口仅序列化透传）
- [ ] 2.4 调用 `/shellConfig/save` 保存部分参数值后重新 query：已保存值正确回显，未保存参数回显默认值；同 type 下其他分组（group_control_vrv 等）存量数据不受影响
- [ ] 2.5 回归验证既有两个主Tab：scene=control 查询既有 groups 的返回结构与变更前一致；`queryByInfoCode`、`batchQueryConfigByObjectId` 对既有参数行为不变
- [x] 2.6 确认 22 个新 code 在 `ShellConfigItemEnum` 全集中唯一，无与既有 code 冲突（改动前 `control_vent` 前缀全库零匹配，改动后 ItemEnum/ExtendEnum 各 22 条一一对应）
- [ ] 2.7 与前端确认契约：groups 过滤参数、单选枚举静态选项列表由前端按 code 维护（后端不下发）

## 3. 文档同步

- [ ] 3.1 向需求方/前端同步 22 个参数的最终 code 清单（design.md D5 表），code 定案即视为与前端联调契约
