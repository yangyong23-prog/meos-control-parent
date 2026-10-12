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
# ------------------------- object_info_code_template_type -------------------------
call drop_table_index('object_info_code_template_type', 'object_info_code_template_type_pk');
call edit_table_column('object_info_code_template_type', 'info_code_filter',
                       'alter table `object_info_code_template_type` modify `info_code_filter` text null comment ''信息点筛选'';');