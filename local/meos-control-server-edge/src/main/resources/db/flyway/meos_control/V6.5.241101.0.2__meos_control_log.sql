update rc_running_log set first_level_category = 'ACCC' where first_level_category = 'coldstation';
update rc_running_log set first_level_category = 'ACCI' where first_level_category = 'cool_hot_system';
update rc_running_log set first_level_category = 'ACCH' where first_level_category = 'hotstation';
update rc_running_log set first_level_category = 'AIR_TERMINAL' where first_level_category = 'air_conditioning_terminal';


update rc_running_log set second_level_category = 'coldstation_device_exception' where second_level_category = 'order_fail_alarm_cold_result';
update rc_running_log set second_level_category = 'coldstation_device_exception' where second_level_category = 'online_status_cold_result';
update rc_running_log set second_level_category = 'hot_device_exception' where second_level_category = 'hot_order_fail_alarm_alarm';
update rc_running_log set second_level_category = 'hot_device_exception' where second_level_category = 'hot_online_status_alarm';

update rc_running_log set third_level_category = 'coldstation_order_fail_alarm_result' where third_level_category = 'order_fail_alarm_cold_alarm';
update rc_running_log set third_level_category = 'coldstation_online_status_result' where third_level_category = 'online_status_cold_alarm';
update rc_running_log set third_level_category = 'hot_order_fail_alarm_result' where third_level_category = 'hot_order_fail_alarm_alarm';
update rc_running_log set third_level_category = 'hot_online_status_result' where third_level_category = 'hot_online_status_alarm';