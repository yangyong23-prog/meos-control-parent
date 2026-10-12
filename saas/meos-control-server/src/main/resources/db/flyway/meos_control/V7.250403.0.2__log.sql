-- 重创分区索引方法
DROP PROCEDURE IF EXISTS auto_check_and_create_partitions;

DELIMITER $$
CREATE PROCEDURE auto_check_and_create_partitions(IN months_to_keep INT)
BEGIN
    DECLARE current_month_start DATE DEFAULT DATE_FORMAT(CURDATE(), '%Y-%m-01');
    DECLARE next_month_start DATE DEFAULT DATE_ADD(current_month_start, INTERVAL 1 MONTH);
    DECLARE partition_current VARCHAR(50); -- 增加长度以容纳数据库名
    DECLARE partition_next VARCHAR(50); -- 增加长度以容纳数据库名
    DECLARE exists_current INT DEFAULT 0;
    DECLARE exists_next INT DEFAULT 0;
    DECLARE cutoff_date DATE;
    DECLARE db_name VARCHAR(64);
    -- 存储当前数据库名

    -- 获取当前数据库名
    SELECT DATABASE() INTO db_name;

    -- 参数校验
    IF months_to_keep IS NULL OR months_to_keep < 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'months_to_keep参数必须为正整数';
    END IF;

    -- 生成分区名称（格式：db名_年月，例如：apccontrol_p202305）
    SET partition_current = CONCAT(db_name, '_p', DATE_FORMAT(current_month_start, '%Y%m'));
    SET partition_next = CONCAT(db_name, '_p', DATE_FORMAT(next_month_start, '%Y%m'));

    -- 计算截止日期
    SET cutoff_date = DATE_SUB(CURDATE(), INTERVAL months_to_keep MONTH);

    -- 检查当前月分区是否存在
    SELECT COUNT(*)
    INTO exists_current
    FROM information_schema.partitions
    WHERE table_name = 'running_log'
      AND partition_name = partition_current
      AND partition_name != 'p_max';

    -- 重组分区
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

    -- 检查下个月分区是否存在
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

    -- 删除旧分区
    SET @drop_partitions = NULL;
    SELECT GROUP_CONCAT(partition_name SEPARATOR ', ')
    INTO @drop_partitions
    FROM information_schema.partitions
    WHERE table_name = 'running_log'
      AND partition_name != 'p_max'
      AND partition_name LIKE CONCAT(db_name, '_p%') -- 增加数据库名前缀匹配
      AND STR_TO_DATE(CONCAT(SUBSTRING(partition_name, LENGTH(db_name) + 3), '01'), '%Y%m%d') < cutoff_date;

    IF @drop_partitions IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE running_log DROP PARTITION ', @drop_partitions);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 保留最近6个月的数据，删除6个月前的分区
CALL auto_check_and_create_partitions(6);