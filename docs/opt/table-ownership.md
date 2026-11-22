# 表归属（运行标准过渡 B6）

2026-11-18 定：运行标准和配置最终归 ids-mpc-operation-standards-server（边缘经 dtp-meos 读取）；meos-control 两个服务只保留监测和手动控制，配置管理在新服务联调通过前保留、之后下线。dtp-meos 与 meos-control 目前在同一个库 `${groupCode}_meos_control`，各自用独立的 Flyway 历史表（我方 `flyway_schema_history`，dtp-meos `flyway_schema_history_dtp_control`）。

## 归属

| 表 | 归属 | 我方可以做什么 | 说明 |
|---|---|---|---|
| `equip_record`、`equip_record_log` | dtp-meos | 现有读写逻辑暂不动；新脚本不得建/改 | dtp-meos `V7.0.0.0__add_demand_result.sql` 也建这两张表，结构与我方不同（见下） |
| `demand_custom_config` | dtp-meos | 只读（B1 判定 STANDRAD_CONFIG） | 建表与写入都在 dtp-meos（`V7.0.0.4__add_custom_table.sql`、`R__init_custom_config.sql`） |
| `standard_compute_result`、`standard_compute_result_detail` | dtp-meos | 不直读，经 dtp-meos `/calculate/listStandardComputeResult*Service` 查询（B4） | dtp-meos `V7.0.0.5__add_standrad_table.sql` |
| `rc_basics_config` | meos-control | 读写 | G2（壳配置映射到新服务）完成前仍归我方，含 `MPC_AGENT_CFG` |
| 其余 meos_control 表 | meos-control | 读写 | 配置类表随 B9 下线 |

## 守护

`local/meos-control-server-edge/src/test/java/com/persagy/apccontrol/opt/OptFlywayTableOwnershipTest.java`：版本高于 `7.260701.0.2` 的 V 脚本和所有 R 脚本不得出现上述 dtp-meos 归属表名，`mvn test` 时失败即报出脚本名。

## 历史脚本里仍涉及 dtp-meos 表的（已执行过，不能改）

改动已执行过的脚本会让现网 Flyway 校验失败，所以只登记：

| 脚本 | 动作 | 与 dtp-meos 的冲突 |
|---|---|---|
| `V6.0.0.20__meos_control.sql` | `CREATE TABLE IF NOT EXISTS equip_record` | 无 `class_code` 列、主键写法不同；dtp-meos 后建时因 IF NOT EXISTS 跳过，再靠 `add_table_column` 补 `class_code` |
| `V6.0.0.43__meos_control.sql` | `drop_table_index('equip_record','idx_info_code')` 后重建为 `(group_code, project_id, exec_status, info_code)`；建 `equip_record_log` | **新盒子若 dtp-meos 先建表**，我方这一步会删掉 dtp-meos 的 `idx_info_code (info_code, exec_status)` 并换成我方定义 |
| `V6.5.241001.0.1__meos_control_equip_record.sql` | 修改 `equip_record` / `equip_record_log` 的 `info_code` 生成列 | 定义与 dtp-meos 相同（varchar(200)），实际无差异 |

建议（待 Blake 定）：新盒子按“dtp-meos 先于 meos-control 执行 Flyway”部署时，上表第二行会改 dtp-meos 的索引。可选做法：(1) 部署顺序定为 meos-control 先执行 Flyway、dtp-meos 后执行（dtp-meos 的 IF NOT EXISTS 会跳过建表，但它要的索引不会建出来）；(2) 请 dtp-meos 在自己的脚本里检查并重建它需要的索引；(3) 两个服务拆库后自然消失（各建各的）。Blake 的目标是 meos-control 不部署时 dtp-meos 也能独立运行，(2) 或 (3) 更符合。在定之前不改历史脚本。

另：边缘 `application-flyway.yml` 里 `spring.flyway.enabled: false`，实际是否执行取决于 Nacos 配置。
