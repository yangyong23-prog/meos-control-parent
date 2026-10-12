# fixbug. V6.0.0.14__meos_control.sql
drop procedure if exists reset_demand_lighting_strategy_config_sum_set;
# fixbug. table rc_basics_config_log error
call drop_table_index('rc_basics_config_log', 'uk');
call add_table_index('rc_basics_config_log', 'idx', 'create index idx on rc_basics_config_log (parent_id);')