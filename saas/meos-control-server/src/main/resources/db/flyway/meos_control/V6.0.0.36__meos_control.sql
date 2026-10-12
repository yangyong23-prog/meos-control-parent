# ------------------------- create procedure add_table_index -------------------------
drop procedure if exists add_table_index;
delimiter $$
create procedure add_table_index(in tableName varchar(64), in indexName varchar(64), in createIndexSql text)
begin
    declare indexCount int;

    select count(*)
    into indexCount
    from information_schema.STATISTICS
    where TABLE_SCHEMA = database()
      and TABLE_NAME = tableName
      and INDEX_NAME = indexName;

    if (indexCount = 0) then
        set @createIndexSql = createIndexSql;
        prepare createIndexStmt from @createIndexSql;
        execute createIndexStmt;
        deallocate prepare createIndexStmt;
    end if;

end $$
delimiter ;

# ------------------------- create procedure drop_table_index -------------------------
drop procedure if exists drop_table_index;
delimiter $$
create procedure drop_table_index(in tableName varchar(64), in indexName varchar(64))
begin
    declare indexCount int;

    select count(*)
    into indexCount
    from information_schema.STATISTICS
    where TABLE_SCHEMA = database()
      and TABLE_NAME = tableName
      and INDEX_NAME = indexName;

    if (indexCount > 0) then
        set @dropIndexSql = concat('drop index ', indexName, ' on ', tableName);
        prepare dropIndexStmt from @dropIndexSql;
        execute dropIndexStmt;
        deallocate prepare dropIndexStmt;
    end if;

end $$
delimiter ;

# ------------------------- create procedure add_table_column -------------------------
drop procedure if exists add_table_column;
delimiter $$
create procedure add_table_column(in tableName varchar(64), in columnName varchar(64), in createColumnSql text)
begin
    declare columnCount int;

    select count(*)
    into columnCount
    from information_schema.COLUMNS
    where TABLE_SCHEMA = database()
      and TABLE_NAME = tableName
      and COLUMN_NAME = columnName;

    if (columnCount = 0) then
        set @updateColumnSql = createColumnSql;
        prepare createColumnStmt from @updateColumnSql;
        execute createColumnStmt;
        deallocate prepare createColumnStmt;
    end if;

end $$
delimiter ;

# ------------------------- create procedure edit_table_column -------------------------
drop procedure if exists edit_table_column;
delimiter $$
create procedure edit_table_column(in tableName varchar(64), in columnName varchar(64), in updateColumnSql text)
begin
    declare columnCount int;

    select count(*)
    into columnCount
    from information_schema.COLUMNS
    where TABLE_SCHEMA = database()
      and TABLE_NAME = tableName
      and COLUMN_NAME = columnName;

    if (columnCount > 0) then
        set @updateColumnSql = updateColumnSql;
        prepare updateColumnStmt from @updateColumnSql;
        execute updateColumnStmt;
        deallocate prepare updateColumnStmt;
    end if;

end $$
delimiter ;

# ------------------------- create procedure drop_table_column -------------------------
drop procedure if exists drop_table_column;
delimiter $$
create procedure drop_table_column(in tableName varchar(64), in columnName varchar(64))
begin
    declare columnCount int;

    select count(*)
    into columnCount
    from information_schema.COLUMNS
    where TABLE_SCHEMA = database()
      and TABLE_NAME = tableName
      and COLUMN_NAME = columnName;

    if (columnCount > 0) then
        set @dropColumnSql = concat('alter table ', tableName, ' drop column ', columnName);
        prepare dropColumnStmt from @dropColumnSql;
        execute dropColumnStmt;
        deallocate prepare dropColumnStmt;
    end if;

end $$
delimiter ;

call edit_table_column('strategy_date_cfg', 'update_user',
                       'alter table strategy_date_cfg modify update_user varchar(64) null comment ''修改人'';');