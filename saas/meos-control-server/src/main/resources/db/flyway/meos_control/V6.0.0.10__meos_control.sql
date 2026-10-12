drop procedure if exists reset_demand_setting_system_id;
delimiter $$
create procedure reset_demand_setting_system_id()
begin
    declare columnId varchar(128);
    declare columnSystemId varchar(128);
    declare cur cursor for
        select id as columnId, system_id as columnSystemId
        from rc_basics_config
        where type = 'demand_control_object';
    declare continue handler for not found set @finished = 1;
    open cur;
    set @finished = 0;
    loop_start:
    loop
        fetch cur into columnId, columnSystemId;
        if @finished = 1 then
            leave loop_start;
        end if;
        if columnSystemId not regexp '^\\[' then
            select columnId, columnSystemId;
            update rc_basics_config set system_id = json_array(columnSystemId) where id = columnId;
        end if;
    end loop loop_start;
    close cur;
end $$
delimiter ;
call reset_demand_setting_system_id();
drop procedure if exists reset_demand_setting_system_id;