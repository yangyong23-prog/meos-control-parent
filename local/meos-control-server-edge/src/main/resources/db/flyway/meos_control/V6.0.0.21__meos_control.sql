REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000247', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'maxPLRSet', '负载率最大值设定', null, null, '%', null, 1, null, null, '34', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000248', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'maxPLRSet', '负载率最大值设定', null, null, '%', null, 1, null, null, '34', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);


update rc_info_code_cfg set iot_decimal = 0 where id = 'IF000246';

update rc_info_code_cfg a join (select distinct class_code, info_code, iot_decimal from rc_info_code_cfg where iot_decimal is not null) b
on a.class_code = b.class_code and a.info_code = b.info_code set a.iot_decimal = b.iot_decimal where a.iot_decimal is null;

update rc_info_code_cfg set remark_config = '{"force": -1}' where id in ('ACAT0093', 'ACAT0138', 'ACAT0172');