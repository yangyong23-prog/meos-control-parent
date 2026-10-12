# 日志记录规范

## 概述

本项目强制要求所有日志记录必须通过 `LogComponent` 接口进行，以确保日志格式统一、便于问题排查和运维监控。

## Requirement: 日志记录接口

所有日志记录 SHALL 通过实现 `LogComponent` 接口进行，禁止直接使用 `log.info()`、`log.error()` 等原生日志方法或
`ControlLogUtil` 等工具类。

### Scenario: 日志组件实现

- **WHEN** 类需要进行日志记录
- **THEN** 该类 SHALL 实现 `LogComponent` 接口
- **AND** 该类 SHALL 实现 `logTag()` 方法返回日志标签

### Scenario: 项目级日志记录

- **WHEN** 需要记录与项目相关的日志
- **THEN** 系统 SHALL 使用 `logProjectInstance().info/warn/error()` 方法
- **AND** 日志 SHALL 自动包含项目上下文信息（projectId）

### Scenario: 日志内容格式

- **WHEN** 记录日志
- **THEN** 日志内容 SHALL 使用中文直写，不需要国际化处理
- **AND** 附加信息 SHALL 使用 `Pair.of("key", value)` 格式传递
- **AND** 多个附加信息 SHALL 使用 `Arrays.asList(Pair.of(...), ...)` 传递

### Scenario: 异常日志记录

- **WHEN** 记录包含异常的日志
- **THEN** 系统 SHALL 使用 `logProjectInstance().error("日志标签", exception, additionalMessages)` 方法
- **AND** 异常对象 SHALL 作为第二个参数传递

## Requirement: 禁止的日志方式

### Scenario: 禁止直接使用 Logger

- **WHEN** 需要记录日志
- **THEN** 系统 SHALL NOT 直接调用 `log.info()`、`log.error()` 等方法

### Scenario: 禁止使用 ControlLogUtil

- **WHEN** 需要记录日志
- **THEN** 系统 SHALL NOT 使用 `ControlLogUtil.info()`、`ControlLogUtil.error()` 等方法

## 示例代码

```java
@Component
public class MyTaskHandler implements LogComponent {

    @Override
    public String logTag() {
        return "XXL-Job定时任务-我的任务";
    }

    public void execute() {
        // 单条附加信息
        logProjectInstance().info("任务开始执行", Pair.of("任务ID", taskId));
        
        // 多条附加信息
        logProjectInstance().info("任务执行完成", 
            Arrays.asList(
                Pair.of("成功数", successCount),
                Pair.of("失败数", failCount)
            ));
        
        // 异常日志
        try {
            // ... 业务逻辑
        } catch (Exception e) {
            logProjectInstance().error("任务执行异常", e, 
                Arrays.asList(
                    Pair.of("任务ID", taskId),
                    Pair.of("异常原因", e.getMessage())
                ));
        }
    }
}
```

## 相关文件

- `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogComponent.java` - 日志组件接口
- `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogComponentProvider.java` - 日志组件提供器
