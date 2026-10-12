drop procedure if exists reset_demand_setting_system_id;
delimiter $$
create procedure reset_demand_setting_system_id()
begin
    declare columnId varchar(64);
    declare systemId varchar(64);
    declare columnResult text;
    declare cur cursor for
        select id as columnId, system_id as systemId, result as columnResult
        from rc_basics_config
        where type = 'demand_control_object';
    declare continue handler for not found set @finished = 1;
    open cur;
    set @finished = 0;
    loop_start:
    loop
        fetch cur into columnId, systemId, columnResult;
        if @finished = 1 then
            leave loop_start;
        end if;
        if systemId is not null and systemId not regexp '^\\[' then
            update rc_basics_config set system_id = json_array(systemId) where id = columnId;
        end if;
        set @jsonSystemIds = columnResult -> '$.systemIds';
        if @jsonSystemIds regexp '^"\\[' then
            update rc_basics_config
            set result = json_set(result, '$.systemIds',
                                  json_array(replace(replace(result -> '$.systemIds', '"[\\\"', ''), '\\\"]"', '')))
            where id = columnId;
        end if;
        set @jsonSystemId = columnResult ->> '$.systemId';
        if @jsonSystemId is not null then
            if @jsonSystemId not regexp '^\\[' then
                update rc_basics_config
                set result = json_set(result, '$.systemIds', json_array(@jsonSystemId))
                where id = columnId;
            else
                update rc_basics_config set result = json_set(result, '$.systemIds', @jsonSystemId) where id = columnId;
            end if;
            update rc_basics_config set result = json_remove(result, '$.systemId') where id = columnId;
        end if;
    end loop loop_start;
    close cur;
end $$
delimiter ;
call reset_demand_setting_system_id();
drop procedure if exists reset_demand_setting_system_id;