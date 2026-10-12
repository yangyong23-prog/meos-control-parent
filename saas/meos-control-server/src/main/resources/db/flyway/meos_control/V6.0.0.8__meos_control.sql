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

    if (indexCount > 0) then
        set @dropIndexSql = concat('drop index ', indexName, ' on ', tableName);
        prepare dropIndexStmt from @dropIndexSql;
        execute dropIndexStmt;
        deallocate prepare dropIndexStmt;
    end if;

    set @createIndexSql = createIndexSql;
    prepare createIndexStmt from @createIndexSql;
    execute createIndexStmt;
    deallocate prepare createIndexStmt;

end $$
delimiter ;