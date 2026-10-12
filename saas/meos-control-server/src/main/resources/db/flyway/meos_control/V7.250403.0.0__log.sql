-- 新增运行日志表
create table if not exists `running_log`
(
    `id`                bigint       NOT NULL COMMENT '主键 id',
    `project_id`        varchar(64)  NOT NULL COMMENT '项目 ID',
    `category` varchar(128) NOT NULL COMMENT '日志分类',
    `type`              int          NOT NULL COMMENT '日志类型  1:自动/2人工/3 报警',
    `object_id`         varchar(128) NOT NULL COMMENT '操作对象id',
    `object_name`       varchar(128)          DEFAULT NULL COMMENT '操作对象名称',
    `state`             int          NOT NULL COMMENT '反馈状态(-1控制结果未知/1成功/2失败/3超时/4终止/5异常)',
    `key_word`          varchar(32)  NOT NULL COMMENT '操作关键字',
    `create_user`       varchar(128) NOT NULL DEFAULT '系统' COMMENT '操作人名称(默认''系统'')',
    `data_time`         datetime     NOT NULL COMMENT '数据时间',
    `create_time`       datetime     NOT NULL COMMENT '创建时间',
    `content`           json         NOT NULL COMMENT '操作内容',
    `class_code`        varchar(32)           DEFAULT NULL COMMENT '对象类型',
    `system_id`         varchar(64)           DEFAULT NULL COMMENT '所属于系统 ID',
    `system_name`       varchar(128) DEFAULT NULL COMMENT '系统名称',
    `system_class_code` varchar(32)  DEFAULT NULL COMMENT '系统编码',
    `effect_start_time` datetime              DEFAULT NULL COMMENT '生效开始时间',
    `effect_end_time`   datetime              DEFAULT NULL COMMENT '生效结束时间',
    `trace_id`          varchar(64)           DEFAULT NULL COMMENT '追踪ID',
    `info_code`         varchar(64)           DEFAULT NULL COMMENT '信息点编码',
    `info_value`        varchar(64)           DEFAULT NULL COMMENT '信息点设定值',
    `source_type`       varchar(64)           DEFAULT NULL COMMENT '来源数据类型(时间表等)',
    PRIMARY KEY (`id`, `data_time`),
    KEY `running_log_project_id_data_time_object_id_type_index` (`project_id`, `data_time`, `object_id`, `type`),
    KEY `running_log_project_id_data_time_category_index` (`project_id`, `data_time`, `category`),
    KEY `running_log_project_id_data_time_type_index` (`project_id`, `data_time`, `type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='感知运行日志表';

-- 创建初始分区
ALTER TABLE running_log
    PARTITION BY RANGE COLUMNS (data_time) (
        PARTITION p_max VALUES LESS THAN (MAXVALUE)
        );

-- 创建自动分区
DROP PROCEDURE IF EXISTS auto_check_and_create_partitions;

DELIMITER $$
CREATE PROCEDURE auto_check_and_create_partitions()
BEGIN
    DECLARE current_month_start DATE DEFAULT DATE_FORMAT(CURDATE(), '%Y-%m-01');
    DECLARE next_month_start DATE DEFAULT DATE_ADD(current_month_start, INTERVAL 1 MONTH);
    DECLARE partition_current VARCHAR(10);
    DECLARE partition_next VARCHAR(10);
    DECLARE exists_current INT DEFAULT 0;
    DECLARE exists_next INT DEFAULT 0;
    DECLARE cutoff_date DATE DEFAULT DATE_SUB(CURDATE(), INTERVAL 1 YEAR);

    -- 生成分区名称
    SET partition_current = CONCAT('p', DATE_FORMAT(current_month_start, '%Y%m'));
    SET partition_next = CONCAT('p', DATE_FORMAT(next_month_start, '%Y%m'));

    -- 检查当前月分区是否存在（过滤 p_max）
    SELECT COUNT(*)
    INTO exists_current
    FROM information_schema.partitions
    WHERE table_name = 'running_log'
      AND partition_name = partition_current
      AND partition_name != 'p_max';

    -- 重组分区（仅当分区不存在时）
    IF exists_current = 0 THEN
        SET @sql_current = CONCAT(
                'ALTER TABLE running_log REORGANIZE PARTITION p_max INTO (',
                'PARTITION ', partition_current, ' VALUES LESS THAN (''',
                DATE_ADD(current_month_start, INTERVAL 1 MONTH), '''),',
                'PARTITION p_max VALUES LESS THAN (MAXVALUE))'
                           );
        PREPARE stmt FROM @sql_current;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;

    -- 检查下个月分区是否存在（同上逻辑）
    SELECT COUNT(*)
    INTO exists_next
    FROM information_schema.partitions
    WHERE table_name = 'running_log'
      AND partition_name = partition_next
      AND partition_name != 'p_max';

    IF exists_next = 0 THEN
        SET @sql_next = CONCAT(
                'ALTER TABLE running_log REORGANIZE PARTITION p_max INTO (',
                'PARTITION ', partition_next, ' VALUES LESS THAN (''',
                DATE_ADD(next_month_start, INTERVAL 1 MONTH), '''),',
                'PARTITION p_max VALUES LESS THAN (MAXVALUE))'
                        );
        PREPARE stmt FROM @sql_next;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;

    -- 删除一年前旧分区
    SET @drop_partitions = NULL;
    SELECT GROUP_CONCAT(partition_name SEPARATOR ', ')
    INTO @drop_partitions
    FROM information_schema.partitions
    WHERE table_name = 'running_log'
      AND STR_TO_DATE(CONCAT(SUBSTRING(partition_name, 2), '01'), '%Y%m%d') < cutoff_date;

    IF @drop_partitions IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE running_log DROP PARTITION ', @drop_partitions);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END
$$
DELIMITER ;


-- 废弃日志历史表 未使用
DROP TABLE IF EXISTS rc_running_log_history;

