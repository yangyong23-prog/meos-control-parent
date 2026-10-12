# 新增判断结果信息点

## Goal

在 InfoCodeEnum 中新增"普通照明反馈调节判断结果"信息点定义，由边缘系统写入，本项目仅读取。

## Parent

`07-29-prd-260801-illum-feedback-circuit`

## Dependencies

无前置依赖，可独立实现。

## Scope

### InfoCodeEnum 新增

- 新增 `GENERAL_LIGHTING_FEEDBACK_ADJUSTMENT_RESULT` 枚举值
- 信息点编码：`generalLightingFeedbackAdjustmentResult`
- 生成新 i18n key UUID

### i18n 新增

- `messages_zh.properties` 新增：`key_<UUID>=普通照明反馈调节判断结果`
- `messages_en.properties` 新增对应英文

### AutoMaticInfoCodeEnum 注册（如需）

- 若该信息点需参与自动指令，在 `AutoMaticInfoCodeEnum` 中注册

## Files

- `common/src/main/java/com/persagy/apccontrol/basic/model/base/InfoCodeEnum.java`
- `common/src/main/resources/i18n/messages_zh.properties`
- `common/src/main/resources/i18n/messages_en.properties`
- `common/.../model/enumtype/AutoMaticInfoCodeEnum.java`（如需）

## Acceptance Criteria

- [ ] `InfoCodeEnum` 新增 `GENERAL_LIGHTING_FEEDBACK_ADJUSTMENT_RESULT`
- [ ] i18n 中英文 key 可正确解析
- [ ] 编译通过
