REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code,
                               class_name, info_code, name, alias, feedback_code, unit, scale, iot_decimal,
                               max, min, ordered, group_info, parameter_type, display_position, force_info,
                               data_source, extend_info, remark_config)
VALUES ('VIRACCC00001', '0', '0', 'info', null, 'ACCC', 'ACCCOT', '冷却塔', 'waterOutTemp', '出塔水温',
        null, null, '℃', null, 1, null, null, '1', null, '运行参数', null, null, null, null, null);


-- 新增设备信息组配置
REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_type, name, alias, ordered, group_info) VALUES
('GPYX002', '0', '0', 'grouping', 'ACCC',  '设备状态', null, '1', 'GPYX001');

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_type, name, alias, ordered, group_info) VALUES
('GPYX003', '0', '0', 'grouping', 'ACCC',  '蒸发器', null, '2', 'GPYX001');

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_type, name, alias, ordered, group_info) VALUES
('GPYX004', '0', '0', 'grouping', 'ACCC',  '冷凝器', null, '3', 'GPYX001');

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_type, name, alias, ordered, group_info) VALUES
('GPYX005', '0', '0', 'grouping', 'ACCC',  '压缩机', null, '4', 'GPYX001');

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_type, name, alias, ordered, group_info) VALUES
('GPYX006', '0', '0', 'grouping', 'ACCC',  '电机', null, '5', 'GPYX001');

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_type, name, alias, ordered, group_info) VALUES
('GPYX007', '0', '0', 'grouping', 'ACCC',  '排气装置', null, '6', 'GPYX001');

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_type, name, alias, ordered, group_info) VALUES
('GPYX008', '0', '0', 'grouping', 'ACCC',  '运行参数', null, '7', 'GPYX001');


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00001', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'ratedI', '额定电流', null, null, 'kW', null, null, null, null, '4', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00002', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'ratedI', '额定电流', null, null, 'kW', null, null, null, null, '4', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00003', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'designChillWaterFlow', '额定蒸发器冷冻水流量', null, null, 'm³/h', null, null, null, null, '8', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00004', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'designChillWaterFlow', '额定蒸发器冷冻水流量', null, null, 'm³/h', null, null, null, null, '8', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00005', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'designCoolWaterFlow', '额定冷凝器冷却水流量', null, null, 'm³/h', null, null, null, null, '9', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00006', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'designCoolWaterFlow', '额定冷凝器冷却水流量', null, null, 'm³/h', null, null, null, null, '9', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00007', '0', '0', 'info', null, 'ACCC', 'ACCCCC', '离心式冷水机组', 'gateOpening', '导叶开度', null, null, '%', null, 1, null, null, '43', 'GPYX005',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00008', '0', '0', 'info', null, 'ACCC', 'ACCCSC', '螺杆式冷水机组', 'gateOpening', '导叶开度', null, null, '%', null, 1, null, null, '43', 'GPYX005',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00009', '0', '0', 'info', null, 'ACCC', 'ACOTU', '冷却塔控制单元', 'accRunTime', '累计运行时间', null, null, 'h', null, 2, null, null, '5', 'GPLQT001',
'运行参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00010', '0', '0', 'info', null, 'ACCC', 'ACGEHE', '空调换热器', 'designHighFlow', '高温侧设计水流量', null, null, 'm³/h', null, null, null, null, '3', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00011', '0', '0', 'info', null, 'ACCC', 'ACGEHE', '空调换热器', 'designLowFlow', '低温侧设计水流量', null, null, 'm³/h', null, null, null, null, '4', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00012', '0', '0', 'info', null, 'ACCC', 'WESSTP', '温度传感器', 'brand', '品牌', null, null, null, null, null, null, null, '3', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00013', '0', '0', 'info', null, 'ACCC', 'WESSPM', '水压表', 'brand', '品牌', null, null, null, null, null, null, null, '3', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00014', '0', '0', 'info', null, 'ACCC', 'WESSFL', '流量传感器', 'brand', '品牌', null, null, null, null, null, null, null, '3', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00015', '0', '0', 'info', null, 'ACCC', 'WESSHS', '热量表', 'brand', '品牌', null, null, null, null, null, null, null, '3', 'GPJC001',
'基础参数', '["details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00016', '0', '0', 'info', null, 'ACCC', 'ACVVEO', '通断水阀', 'localRemoteStatus', '本地远程状态', null, null, null, null, null, null, null, '3', 'GPYX001',
'运行参数', '["home_page", "details"]', null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('REACCC00017', '0', '0', 'info', null, 'ACCC', 'ACVVEJ', '调节水阀', 'localRemoteStatus', '本地远程状态', null, null, null, null, null, null, null, '3', 'GPYX001',
'运行参数', '["home_page", "details"]', null, null, null, null);

update rc_info_code_cfg set ordered = '5' where id in ('IF000023','IF000201');
update rc_info_code_cfg set ordered = '6' where id in ('IF000024','IF000202');
delete from rc_info_code_cfg where id in ('IF000025','IF000203', 'IF000070', 'IF000089');


update rc_info_code_cfg set group_info = 'GPYX002' where id in ('IF000026','IF000027','IF000028','IF000029','IF000030', 'IF000204', 'IF000205', 'IF000206', 'IF000207', 'IF000208',
                                                                'IF000048', 'IF000226', 'IF000071', 'IF000072', 'IF000073', 'IF000074', 'IF000075', 'IF000122', 'IF000123', 'IF000135');

update rc_info_code_cfg set group_info = 'GPYX003' where id in ('IF000031','IF000209','IF000033','IF000211','IF000034','IF000212','IF000037','IF000215','IF000038','IF000216',
                                                                'IF000042','IF000220','IF000031','IF000031', 'IF000050', 'IF000228', 'IF000051', 'IF000229', 'IF000052', 'IF000230');

update rc_info_code_cfg set group_info = 'GPYX004' where id in ('IF000032', 'IF000210', 'IF000035', 'IF000213', 'IF000036', 'IF000214', 'IF000039', 'IF000217','IF000040', 'IF000218',
                                                                'IF000041', 'IF000219', 'IF000039', 'IF000039', 'IF000039', 'IF000039','IF000049', 'IF000227', 'IF000053', 'IF000231');
update rc_info_code_cfg set group_info = 'GPYX004' where id in ('IF000054', 'IF000232');

update rc_info_code_cfg set group_info = 'GPYX005' where id in ('IF000047', 'IF000225', 'IF000057', 'IF000235', 'IF000058', 'IF000236', 'IF000256', 'IF000277', 'IF000257', 'IF000278');

update rc_info_code_cfg set group_info = 'GPYX006' where id in ('IF000043', 'IF000221', 'IF000044', 'IF000222', 'IF000045', 'IF000223', 'IF000249', 'IF000270','IF000250', 'IF000271',
                                                                'IF000251', 'IF000272', 'IF000252', 'IF000273', 'IF000046', 'IF000224', 'IF000253', 'IF000274');

update rc_info_code_cfg set group_info = 'GPYX007' where id in ('IF000055', 'IF000233', 'IF000056', 'IF000234', 'IF000254', 'IF000275', 'IF000255', 'IF000276');

update rc_info_code_cfg set group_info = 'GPYX008' where id in ('IF000076', 'IF000077', 'IF000078', 'IF000258', 'IF000259', 'IF000260', 'IF000261', 'IF000079', 'IF000262', 'IF000080',
                                                                'IF000124','IF000125', 'IF000126','IF000127', 'IF000128', 'IF000129', 'IF000130', 'IF000131', 'IF000132', 'IF000133');
update rc_info_code_cfg set group_info = 'GPYX008' where id in ('IF000134');