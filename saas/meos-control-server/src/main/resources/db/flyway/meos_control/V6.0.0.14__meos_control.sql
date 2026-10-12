drop procedure if exists reset_demand_lighting_strategy_config_sum_set;
delimiter $$
create procedure reset_demand_lighting_strategy_config_sum_set()
begin
    declare columnId varchar(64);
    declare cur cursor for
        select id from demand_lighting_strategy_config where config_data like '%SUM_SET%';
    declare continue handler for not found set @finished = 1;
    open cur;
    set @finished = 0;
    loop_start:
    loop
        fetch cur into columnId;
        if @finished = 1 then
            leave loop_start;
        end if;
        update demand_lighting_strategy_config
        set config_data = replace(config_data, 'SUM_SET', 'SUN_SET')
        where id = columnId;
    end loop loop_start;
    close cur;
end $$
delimiter ;
call reset_demand_lighting_strategy_config_sum_set();
drop procedure if exists reset_demand_setting_system_id;