# 任务清单

## 核心实现

### 1. InfoCodeLinkControlHandler.checkTriggerFormulaInfo() - 配置检查日志（common 模块）

- [x] 添加配置检查的 DEBUG 级别日志
    - [x] 无配置场景（infoCodeCfg == null 或 extend_info == null）
    - [x] 离心式冷水机组(ACCCCC) - 单点联动模式不匹配
    - [x] 冷却塔控制单元(ACCCSC) - 单点联动模式不匹配
    - [x] 冷却塔(ACOTU) - 单点控制模式
    - [x] 空调换热器(ACGEHE) - 单点控制模式
    - [x] 通过检查场景
- [x] 添加 ControlLogUtil import
- [x] 添加 @Slf4j 注解

### 2. ComputeIotDataHandler.computeInfoCodeDataMap() - 计算结果处理

- [x] 修改跳过日志的原因说明
    - [x] 改为"配置检查未通过"
    - [x] 引导查看配置检查日志
- [x] 添加计算成功的 INFO 级别日志
    - [x] 记录设备ID、触发信息点
    - [x] 记录结果信息点（rcRunStatus）
    - [x] 记录计算值及中文名称
- [x] 添加计算失败的 ERROR 级别日志
    - [x] 记录设备ID、触发信息点
    - [x] 记录结果信息点
    - [x] 记录失败原因（计算结果为null）

### 3. ComputeIotDataHandler.pushMessage() - 推送结果日志

- [x] 添加 rcRunStatus 推送日志
    - [x] 使用 try-catch 包裹推送逻辑
    - [x] 仅对 rcRunStatus 信息点记录
    - [x] 不影响其他信息点的推送逻辑
- [x] 添加推送成功的 INFO 级别日志
    - [x] 记录设备ID、信息点
    - [x] 记录值及中文名称
    - [x] 记录数据状态
- [x] 添加推送失败的 ERROR 级别日志
    - [x] 记录设备ID、信息点
    - [x] 记录值及中文名称
    - [x] 记录异常信息和堆栈

### 4. ComputeIotDataHandler.checkControlRealStatus() - 控制指令计算

- [x] 记录进行中状态的决策路径
- [x] 记录完成状态的决策路径
- [x] 记录在线状态检查逻辑
- [x] 增强异常日志（包含完整上下文）

### 5. ComputeIotDataHandler.getTriggerFormulaInfoCodeValue() - 公式计算

- [x] 记录公式变量值
- [x] 记录公式执行结果
- [x] 增强异常日志

### 6. 辅助方法

- [x] 添加 `getStatusName()` 方法
    - [x] 状态值映射：-1=无效, 0=停止, 1=运行, 2=开启中, 3=关闭中

## 测试验证

- [x] 本地测试日志输出格式（通过代码审查验证）
- [x] 验证日志包含所有必要字段（通过代码审查验证）
- [x] 测试不同场景的日志输出：（通过代码审查验证）
    - [x] 进行中状态（dataStatus=2）- checkControlRealStatus() L492-504
    - [x] 完成状态（dataStatus=1）- checkControlRealStatus() L507-525
    - [x] 设备离线场景 - checkControlRealStatus() L511-512
    - [x] 跳过计算场景 - computeInfoCodeDataMap() L362-368
    - [x] 异常场景 - getTriggerFormulaInfoCodeValue() L464-469, checkControlRealStatus() L528-533

## 文档

- [x] 创建 proposal.md
- [x] 创建 design.md
- [x] 创建 spec.md
- [x] 创建 tasks.md
- [ ] 编写日志检索指南（可选，可单独文档）

## 部署准备

- [x] 代码审查（已完成全面代码审查）
- [ ] 测试环境部署验证
- [ ] 日志量评估（确认符合预期）
- [ ] 生产环境部署

## 后续优化（可选）

- [ ] 状态变化检测日志
- [ ] 状态持续时间告警
- [ ] Nacos 动态日志级别配置
- [ ] 日志分析和可视化工具
