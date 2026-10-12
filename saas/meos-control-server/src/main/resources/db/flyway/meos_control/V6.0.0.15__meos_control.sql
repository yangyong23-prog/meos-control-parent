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