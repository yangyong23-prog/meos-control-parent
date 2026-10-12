# 执行计划：壳配置 same_time 时前端时间参数未生效修复 (UYAI-1348)

## 修改清单

全部改动收敛在 1 个文件：`common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service/impl/DtpRwdClientServiceImpl.java`

### 1. `queryIpDataOnMoment(4参)`（约 L1170-1183）分支重组

现状 → 目标（见 design.md §2.1）：

- if 条件由 `isRemoveDefaultIpType()` 改为 `isRemoveDefaultIpType() && isLatestDataQueryLogic()`
- if 分支删除 `if (this.isKeepDefaultIpType()) { periodDTO.setIpType("15min"); }`（死代码）
- 其余（时间窗口计算、else 分支 15min + substitutionParameter）原样保留

### 2. `queryIpDataOnMomentInOneHour`（约 L1217-1232）分支重组

同上，else 分支（15min + -1h~now + substitutionParameter）原样保留

### 3. 私有辅助方法（约 L1569-1592）

`isKeepDefaultIpType()` 更名 `isLatestDataQueryLogic()` 并反转语义：

- 返回值：`"latest".equals(value)`（读取异常/缺失/未知值回退 false）
- Javadoc 重写：描述 latest 语义 + 回退行为
- 同步替换两处调用点（L1176、L1223）

`getPersistentDataInterval()` 不动；`substitutionParameter()` 不动

## 执行顺序

1. 改动前 `git diff` 留底（分支当前 clean）
2. 按上述清单做 SearchReplace 修改
3. 静态自查：GetProblems 无错误；grep 全仓确认 `isKeepDefaultIpType` 已无引用、`isLatestDataQueryLogic` 引用点为 2 处调用 + 1 处定义
4. 编译验证（模块编译或 IDEA 构建）
5. review gate：对照 prd.md 行为矩阵人工复查差异点

## 验证命令（回归矩阵）

本地启动后按 prd.md R2/R3 验证；关键场景：

| # | 场景 | 期望 |
|---|---|---|
| 1 | same_time + 标识 + 传 IPDataStartTime/IPDataEndTime（冷源实时页面） | DMP 请求时间=前端传值，返回该时刻固定分精度数据（UYAI-1348 复现项） |
| 2 | same_time + 标识 + 不传参数 | 4参版 15min 无窗口；InOneHour 版 -1h~now |
| 3 | latest + 标识（interval=1h） | [now-1h, now] 不传精度 |
| 4 | latest + 标识（interval=168h） | [now-168h, now] 不传精度 |
| 5 | 任意配置 + 无标识 | 与改造前一致 |
| 6 | SSE 订阅带标识：same_time / latest | 推送查询分别走旧逻辑 / latest 逻辑 |
| 7 | 显式分精度（DAY/5min 调用） | 不受影响 |

场景 3/4/5 为防回归项（08-26 已验收）；场景 1/2 为本 bug 修复项；场景 6/7 抽查。

## 回滚点

- 代码回滚：单文件 revert（`git checkout -- <file>` 或反向 patch）
- 运行回滚：壳配置页面切回 same_time 即恢复旧行为（无需发版）
