REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('ACCCACCC00001', '0', '0', 'info', null, 'ACCC', 'ACCC', '中央供冷系统', 'sysSwitchSet', '系统开关设定', null, null, null, null, null, null, null, '20', null,
'设定参数', null, null, '[{"code": "0", "name": "关闭"}, {"code": "1", "name": "开启"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('ACCIACCI00001', '0', '0', 'info', null, 'ACCI', 'ACCI', '中央冷热源系统', 'sysSwitchSet', '系统开关设定', null, null, null, null, null, null, null, '20', null,
'设定参数', null, null, '[{"code": "0", "name": "关闭"}, {"code": "1", "name": "开启"}]', null, null);


update class_define_config set extend_info = null where class_code = 'ACCI';
update class_define_config set extend_info = '{"ACCI": {"manualAutoSet": true}}' where class_code = 'ACAHC';
update class_define_config set extend_info = '{"ACCI": {"manualAutoSet": true}}' where class_code = 'ACCHCH';
update class_define_config set extend_info = '{"ACCC": {"manualAutoSet": true}, "ACCI": {"manualAutoSet": true}}' where class_code = 'ACVVEJ';
update class_define_config set extend_info = '{"ACCC": {"groupName": "设备", "manualAutoSet": true}, "ACCI": {"manualAutoSet": true}}' where class_code = 'ACVVEO';

update rc_basics_config set result = '[{"valueMin":null,"isIncludeMin":null,"valueMax":18,"isIncludeMax":false,"range":"<18"},{"valueMin":18,"isIncludeMin":true,"valueMax":20,"isIncludeMax":false,"range":"[18, 20)"},{"valueMin":20,"isIncludeMin":true,"valueMax":22,"isIncludeMax":false,"range":"[20, 22)"},{"valueMin":22,"isIncludeMin":true,"valueMax":24,"isIncludeMax":false,"range":"[22, 24)"},{"valueMin":24,"isIncludeMin":true,"valueMax":26,"isIncludeMax":false,"range":"[24, 26)"},{"valueMin":26,"isIncludeMin":true,"valueMax":28,"isIncludeMax":true,"range":"[26, 28]"},{"valueMin":28,"isIncludeMin":false,"valueMax":null,"isIncludeMax":null,"range":">28"},{"valueMin":null,"isIncludeMin":null,"valueMax":null,"isIncludeMax":null,"range":"无数据"}]' where type = 'room_temp_range';