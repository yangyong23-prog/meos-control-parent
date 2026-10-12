REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code,
                               class_name, info_code, name, alias, feedback_code, unit, scale, iot_decimal,
                               max, min, ordered, group_info, parameter_type, display_position, force_info,
                               data_source, extend_info, remark_config)
VALUES ('ACCCACCPC03', '0', '0', 'info', null, 'ACCC', 'ACCPC', '冷热共用水泵控制组', 'branchDetaTemp', '供回水温差',
        null, null, '℃', null, 1, null, null, '1', null, '运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code,
                               class_name, info_code, name, alias, feedback_code, unit, scale, iot_decimal,
                               max, min, ordered, group_info, parameter_type, display_position, force_info,
                               data_source, extend_info, remark_config)
VALUES ('ACCCACCPC04', '0', '0', 'info', null, 'ACCC', 'ACCPC', '冷热共用水泵控制组', 'branchDetaPress', '供回水压差',
        null, null, 'kPa', '1000', 1, null, null, '2', null, '运行参数', null, null, null, null, null);


update rc_info_code_cfg set ordered = '20' where id = 'IF000045';
update rc_info_code_cfg set ordered = '25' where id = 'IF000046';
update rc_info_code_cfg set ordered = '27' where id = 'IF000047';
update rc_info_code_cfg set ordered = '28' where id = 'IF000048';
update rc_info_code_cfg set ordered = '29' where id = 'IF000049';
update rc_info_code_cfg set ordered = '30' where id = 'IF000050';
update rc_info_code_cfg set ordered = '31' where id = 'IF000051';
update rc_info_code_cfg set ordered = '32' where id = 'IF000052';
update rc_info_code_cfg set ordered = '33' where id = 'IF000053';
update rc_info_code_cfg set ordered = '34' where id = 'IF000054';
update rc_info_code_cfg set ordered = '35' where id = 'IF000055';
update rc_info_code_cfg set ordered = '36' where id = 'IF000056';
update rc_info_code_cfg set ordered = '39' where id = 'IF000057';
update rc_info_code_cfg set ordered = '40' where id = 'IF000058';
update rc_info_code_cfg set ordered = '43' where id = 'IF000247';


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000249', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'u', '运行电压', null, null, 'V', null, 1, null, null, '21', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000250', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'aPhaseI', 'A相电流', null, null, 'A', null, 1, null, null, '22', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000251', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'bPhaseI', 'B相电流', null, null, 'A', null, 1, null, null, '23', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000252', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'cPhaseI', 'C相电流', null, null, 'A', null, 1, null, null, '24', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000253', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'elecConsumP', '耗电功率', null, null, 'kW', null, 1, null, null, '26', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000254', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'compressInTemp', '压缩机吸气温度', null, null, '℃', null, 1, null, null, '37', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000255', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'compressInPress', '压缩机吸气压力', null, null, 'kPa', '1000', 1, null, null, '38', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000256', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'accCompressStartTimes', '压缩机累计启动次数', null, null, '次', null, 0, null, null, '41', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000257', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'accRunTime', '累计运行时间', null, null, 'h', null, 2, null, null, '42', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);



update rc_info_code_cfg set ordered = '13' where id = 'IF000079';
update rc_info_code_cfg set ordered = '15' where id = 'IF000080';
update rc_info_code_cfg set ordered = '13' where id = 'IF000098';
update rc_info_code_cfg set ordered = '15' where id = 'IF000099';



REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000258', '0', '0', 'info', null, 'ACCC', 'ACCCCP', '供冷冷冻水泵', 'u', '运行电压', null, null, 'V', null, 1, null, null, '9', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000259', '0', '0', 'info', null, 'ACCC', 'ACCCCP', '供冷冷冻水泵', 'aPhaseI', 'A相电流', null, null, 'A', null, 1, null, null, '10', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000260', '0', '0', 'info', null, 'ACCC', 'ACCCCP', '供冷冷冻水泵', 'bPhaseI', 'B相电流', null, null, 'A', null, 1, null, null, '11', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000261', '0', '0', 'info', null, 'ACCC', 'ACCCCP', '供冷冷冻水泵', 'cPhaseI', 'C相电流', null, null, 'A', null, 1, null, null, '12', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000262', '0', '0', 'info', null, 'ACCC', 'ACCCCP', '供冷冷冻水泵', 'elecConsumP', '耗电功率', null, null, 'kW', null, 1, null, null, '14', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000263', '0', '0', 'info', null, 'ACCC', 'ACCCOP', '供冷冷却水泵', 'u', '运行电压', null, null, 'V', null, 1, null, null, '9', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000264', '0', '0', 'info', null, 'ACCC', 'ACCCOP', '供冷冷却水泵', 'aPhaseI', 'A相电流', null, null, 'A', null, 1, null, null, '10', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000265', '0', '0', 'info', null, 'ACCC', 'ACCCOP', '供冷冷却水泵', 'bPhaseI', 'B相电流', null, null, 'A', null, 1, null, null, '11', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000266', '0', '0', 'info', null, 'ACCC', 'ACCCOP', '供冷冷却水泵', 'cPhaseI', 'C相电流', null, null, 'A', null, 1, null, null, '12', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000267', '0', '0', 'info', null, 'ACCC', 'ACCCOP', '供冷冷却水泵', 'elecConsumP', '耗电功率', null, null, 'kW', null, 1, null, null, '14', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000268', '0', '0', 'info', null, 'ACCC', 'ACCCOF', '冷却塔风机', 'elecConsumP', '耗电功率', null, null, 'kW', null, 1, null, null, '10', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000269', '0', '0', 'info', null, 'ACCC', 'ACGEFP', '补水泵', 'elecConsumP', '耗电功率', null, null, 'kW', null, 1, null, null, '6', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);



update rc_info_code_cfg set ordered = '25' where id = 'IF000224';
update rc_info_code_cfg set ordered = '27' where id = 'IF000225';
update rc_info_code_cfg set ordered = '28' where id = 'IF000226';
update rc_info_code_cfg set ordered = '29' where id = 'IF000227';
update rc_info_code_cfg set ordered = '30' where id = 'IF000228';
update rc_info_code_cfg set ordered = '31' where id = 'IF000229';
update rc_info_code_cfg set ordered = '32' where id = 'IF000230';
update rc_info_code_cfg set ordered = '33' where id = 'IF000231';
update rc_info_code_cfg set ordered = '34' where id = 'IF000232';
update rc_info_code_cfg set ordered = '35' where id = 'IF000233';
update rc_info_code_cfg set ordered = '36' where id = 'IF000234';
update rc_info_code_cfg set ordered = '39' where id = 'IF000235';
update rc_info_code_cfg set ordered = '40' where id = 'IF000236';
update rc_info_code_cfg set ordered = '43' where id = 'IF000248';


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000270', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'u', '运行电压', null, null, 'V', null, 1, null, null, '21', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000271', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'aPhaseI', 'A相电流', null, null, 'A', null, 1, null, null, '22', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000272', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'bPhaseI', 'B相电流', null, null, 'A', null, 1, null, null, '23', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000273', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'cPhaseI', 'C相电流', null, null, 'A', null, 1, null, null, '24', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000274', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'elecConsumP', '耗电功率', null, null, 'kW', null, 1, null, null, '26', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000275', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'compressInTemp', '压缩机吸气温度', null, null, '℃', null, 1, null, null, '37', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000276', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'compressInPress', '压缩机吸气压力', null, null, 'kPa', '1000', 1, null, null, '38', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000277', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'accCompressStartTimes', '压缩机累计启动次数', null, null, '次', null, 0, null, null, '41', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('IF000278', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'accRunTime', '累计运行时间', null, null, 'h', null, 2, null, null, '42', 'GPYX001',
'运行参数', '["details"]', null, null, null, null);