# JavaDoc 注释规范

## 概述

本项目要求所有 Java 代码必须具有完整的 JavaDoc 注释，注释内容必须与代码实现保持一致，便于代码维护和 API 文档生成。

## Requirement: JavaDoc 完整性

所有类、接口、方法 SHALL 具有完整的 JavaDoc 注释，注释内容必须准确描述代码功能。

### Scenario: 类级别注释

- **WHEN** 定义类或接口
- **THEN** 类 SHALL 包含 JavaDoc 注释
- **AND** 注释 SHALL 描述类的职责和用途
- **AND** 注释 SHALL 包含 `@author` 标签，标注作者
- **AND** 注释 SHALL 包含 `@since` 标签，标注引入版本

### Scenario: 方法级别注释

- **WHEN** 定义 public 或 protected 方法
- **THEN** 方法 SHALL 包含 JavaDoc 注释
- **AND** 注释 SHALL 描述方法的功能
- **AND** 如有参数，注释 SHALL 包含 `@param` 标签
- **AND** 如有返回值，注释 SHALL 包含 `@return` 标签
- **AND** 如可能抛出异常，注释 SHALL 包含 `@throws` 标签
- **AND** 注释 SHALL 包含 `@since` 标签，标注引入版本

### Scenario: 字段级别注释

- **WHEN** 定义 public 或 protected 常量或字段
- **THEN** 字段 SHALL 包含 JavaDoc 注释
- **AND** 注释 SHALL 描述字段的含义和用途

## Requirement: since 标签规范

`@since` 标签用于标注代码引入的版本号，版本号 SHALL 与当前 Git 分支名称匹配，便于追溯代码来源。

### Scenario: 版本号定义

- **WHEN** 添加 `@since` 标签
- **THEN** 版本号 SHALL 定义在 `VersionConstant` 中
- **AND** 版本号格式 SHALL 为 `{主版本}.{YYMM}.{迭代}.{补丁}`

**版本号格式说明：**

| 组成部分 | 说明         | 示例                |
|------|------------|-------------------|
| 主版本  | 主版本号       | `7`               |
| YYMM | 年份后两位 + 月份 | `2604` 表示 2026年4月 |
| 迭代   | 当月第几次迭代    | `01` 或 `02`       |
| 补丁   | 补丁版本号      | `0`               |

**示例：** `7.260402.0` 表示 7.x 主版本，2026年4月第2次迭代，无补丁

### Scenario: 版本号与 Git 分支对应

- **WHEN** 在特定迭代分支上开发
- **THEN** `@since` 标签值 SHALL 与 Git 分支名称中的版本号一致

**分支命名规范：**

```
feature/{版本号}-{功能描述}
```

**示例：**

- 分支名：`feature/7.260402.0-rc-runstatus`
- 对应版本号：`7.260402.0`

### Scenario: 新增类

- **WHEN** 新增类或接口
- **THEN** `@since` 标签值 SHALL 为当前迭代版本号
- **AND** 版本号 SHALL 从 `VersionConstant` 中获取常量值

### Scenario: 新增方法

- **WHEN** 在已有类中新增方法
- **THEN** `@since` 标签值 SHALL 为当前迭代版本号
- **AND** 不修改类的原有 `@since` 标签

### Scenario: 修改方法

- **WHEN** 修改已有方法的行为或参数
- **THEN** 注释 SHALL 更新以反映变化
- **AND** 保持原 `@since` 标签不变
- **AND** 如有重大变更，可添加 `@deprecated` 标签说明

## Requirement: 注释与代码一致性

### Scenario: 参数描述

- **WHEN** 方法的参数变化（新增、删除、重命名）
- **THEN** JavaDoc 中的 `@param` 标签 SHALL 同步更新

### Scenario: 返回值描述

- **WHEN** 方法的返回值类型或含义变化
- **THEN** JavaDoc 中的 `@return` 标签 SHALL 同步更新

### Scenario: 异常描述

- **WHEN** 方法抛出的异常变化
- **THEN** JavaDoc 中的 `@throws` 标签 SHALL 同步更新

## Requirement: 禁止的注释方式

### Scenario: 禁止空注释

- **WHEN** 编写代码
- **THEN** 系统 SHALL NOT 使用空的 JavaDoc 注释（如 `/** */`）

### Scenario: 禁止过时注释

- **WHEN** 代码逻辑变更
- **THEN** 系统 SHALL NOT 保留与代码不符的过时注释

### Scenario: 禁止无意义注释

- **WHEN** 编写 JavaDoc
- **THEN** 注释 SHALL NOT 仅重复方法名或参数名
- **AND** 注释 SHALL 提供有意义的说明

## 示例代码

### 类级别注释

```java
/**
 * rcRunStatus 补偿任务处理器
 * 定时检查处于过渡状态（开启中=2，关闭中=3）的设备，
 * 根据 runStatus 信息点更新 rcRunStatus
 *
 * @author JiYinchuan
 * @since 7.260402.0
 */
@Component
public class RcRunStatusCompensationTaskHandler implements LogComponent {
    // ...
}
```

### 方法级别注释

```java
/**
 * 处理单个设备的补偿逻辑
 *
 * @param objectId     设备ID
 * @param trackingKey  跟踪标记Key
 * @return true-已处理（更新并清除标记），false-跳过（保留标记）
 * @since 7.260402.0
 */
private boolean processDevice(String objectId, String trackingKey) {
    // ...
}
```

### 常量注释

```java
/**
 * rcRunStatus补偿跟踪标记key前缀
 */
public static final String APC_RC_RUNSTATUS_TRACKING_KEY = APC + CommonConst.COLON + "rc_runstatus_tracking";
```

## 相关文件

- `common/src/main/java/com/persagy/apccontrol/basic/constant/VersionConstant.java` - 版本常量定义
