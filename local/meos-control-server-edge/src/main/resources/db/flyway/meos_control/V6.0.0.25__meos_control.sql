UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP003' WHERE id = 'ACCIIF001';
UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP003' WHERE id = 'ACCIIF002';
UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP005' WHERE id = 'ACCIIF014';
UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP005' WHERE id = 'ACCIIF015';
UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP005' WHERE id = 'ACCIIF016';
UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP005' WHERE id = 'ACCIIF017';
UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP005' WHERE id = 'ACCIIF018';
UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP005' WHERE id = 'ACCIIF019';
UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP005' WHERE id = 'ACCIIF020';
UPDATE rc_info_code_cfg SET group_info  = 'ACCIGP005' WHERE id = 'ACCIIF021';


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000279', '0', '0', 'info', null, 'ACCC', 'ACCCCP', '供冷冷冻水泵', 'ratedFlow', '额定流量', null, null, 'm³/h', null, null, null, null, '7', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000280', '0', '0', 'info', null, 'ACCC', 'ACCCCP', '供冷冷冻水泵', 'ratedHead', '额定扬程', null, null, 'm', null, null, null, null, '8', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000281', '0', '0', 'info', null, 'ACCC', 'ACCCCP', '供冷冷冻水泵', 'ratedPumpEff', '水泵额定效率', null, null, '%', null, null, null, null, '9', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000282', '0', '0', 'info', null, 'ACCC', 'ACCCOP', '供冷冷却水泵', 'ratedFlow', '额定流量', null, null, 'm³/h', null, null, null, null, '7', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000283', '0', '0', 'info', null, 'ACCC', 'ACCCOP', '供冷冷却水泵', 'ratedHead', '额定扬程', null, null, 'm', null, null, null, null, '8', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000284', '0', '0', 'info', null, 'ACCC', 'ACCCOP', '供冷冷却水泵', 'ratedPumpEff', '水泵额定效率', null, null, '%', null, null, null, null, '9', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);