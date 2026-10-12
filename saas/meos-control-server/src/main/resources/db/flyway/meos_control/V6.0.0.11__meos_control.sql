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

    if (columnCount > 0) then
        set @dropColumnSql = concat('alter table ', tableName, ' drop column ', columnName);
        prepare dropColumnStmt from @dropColumnSql;
        execute dropColumnStmt;
        deallocate prepare dropColumnStmt;
    end if;

    set @createColumnSql = createColumnSql;
    prepare createColumnStmt from @createColumnSql;
    execute createColumnStmt;
    deallocate prepare createColumnStmt;

end $$
delimiter ;

call add_table_column('demand_lighting_strategy_config', 'instance_type',
                      'alter table demand_lighting_strategy_config add instance_type varchar(32) not null comment ''实例类型'' after project_id;');
call add_table_index('demand_lighting_strategy_config', 'demand_lighting_strategy_config_pk',
                     'create unique index demand_lighting_strategy_config_pk on demand_lighting_strategy_config (project_id, lighting_type, calendar_tag, instance_type, strategy_type, st, building_id, floor_id);')