-- 表索引更新开始  ----------------------
-- 创建新增索引的方法  targetTableName表名称  targetIndexName需要判断的索引名称  SqlStr执行语句
DROP PROCEDURE IF EXISTS TEC_ADD_INDEX;
DELIMITER $$
CREATE PROCEDURE TEC_ADD_INDEX(targetTableName VARCHAR(50), targetIndexName VARCHAR(50), SqlStr VARCHAR(4000))
BEGIN
    IF NOT EXISTS(SELECT *
                  FROM information_schema.statistics
                  WHERE table_schema = DATABASE()
                    AND table_name = targetTableName
                    AND index_name = targetIndexName) THEN
        -- 执行命令
        IF (SqlStr <> '') THEN
            SET @SQL1 = SqlStr;
PREPARE stmt1 FROM @SQL1;
EXECUTE stmt1;
END IF;
END IF;
END $$
DELIMITER ;

-- 表字段更新开始  ----------------------
-- 创建字段新增 更新 删除的判断方法 先校验字段不存在才进行操作
DROP PROCEDURE IF EXISTS Pro_Temp_ColumnWork;
DELIMITER $$
-- 1表示新增列,2表示修改列类型,3表示删除列
CREATE PROCEDURE Pro_Temp_ColumnWork(TableName VARCHAR(50), ColumnName VARCHAR(50), SqlStr VARCHAR(4000), CType INT)
BEGIN
    DECLARE Rows1 INT;
    SET Rows1 = 0;
SELECT COUNT(*)
INTO Rows1
FROM INFORMATION_SCHEMA.Columns
WHERE table_schema = DATABASE()
  AND table_name = TableName
  AND column_name = ColumnName;
-- 新增列
IF (CType = 1 AND Rows1 <= 0) THEN
        SET SqlStr := CONCAT('ALTER TABLE ', TableName, ' ADD COLUMN ', ColumnName, ' ', SqlStr);
-- 修改列类型
    ELSEIF (CType = 2 AND Rows1 > 0) THEN
        SET SqlStr := CONCAT('ALTER TABLE ', TableName, ' MODIFY COLUMN ', ColumnName, ' ', SqlStr);
-- 删除列
    ELSEIF (CType = 3 AND Rows1 > 0) THEN
        SET SqlStr := CONCAT('ALTER TABLE  ', TableName, ' DROP COLUMN  ', ColumnName);
-- 字段名称修改
    ELSEIF (CType = 4 AND Rows1 > 0) THEN
        SET SqlStr := CONCAT('ALTER TABLE  ', TableName, ' CHANGE COLUMN  ', ColumnName, ' ', SqlStr);
ELSE
        SET SqlStr := '';
END IF;
-- 执行命令
    IF (SqlStr <> '') THEN
        SET @SQL1 = SqlStr;
PREPARE stmt1 FROM @SQL1;
EXECUTE stmt1;
END IF;
END $$
DELIMITER ;

