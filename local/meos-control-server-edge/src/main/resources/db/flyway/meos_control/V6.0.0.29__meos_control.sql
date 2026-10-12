delete from rc_info_code_cfg where class_code = 'ACATAH';

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00001', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'localRemoteStatus', '本地远程状态', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "本地"}, {"code": "1", "name": "远程"}]', null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00002', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'manualAutoStatus', '手自动状态', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "手动"}, {"code": "1", "name": "自动"}]', null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00003', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'onlineStatus', '在线状态', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "离线"}, {"code": "1", "name": "在线"}]', '{"variables":[{"dataType":0,"infoCode":"runStatus"},{"dataType":0,"infoCode":"onlineStatus"}],"sourceIotType":1,"triggerFormula":"runStatus == nil || onlineStatus == nil ? 0 : onlineStatus","triggerInfoCode":"runStatus"}', null);

update rc_info_code_cfg set extend_info = '{"variables":[{"dataType":0,"infoCode":"runStatus"},{"dataType":0,"infoCode":"onlineStatus"}],"sourceIotType":1,"triggerFormula":"runStatus == nil || onlineStatus == nil ? 0 : onlineStatus","triggerInfoCode":"runStatus"}' where id in ('ACAT0022', 'ACAT0085');


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00004', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'runStatus', '运行状态', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}]', '{"variables":[{"dataType":0,"infoCode":"runStatus"},{"dataType":0,"infoCode":"onlineStatus"}],"sourceIotType":1,"triggerFormula":"runStatus == nil || onlineStatus == nil || onlineStatus == 0 ? -1 : runStatus","triggerInfoCode":"runStatus"}', null);

update rc_info_code_cfg set extend_info = '{"variables":[{"dataType":0,"infoCode":"runStatus"},{"dataType":0,"infoCode":"onlineStatus"}],"sourceIotType":1,"triggerFormula":"runStatus == nil || onlineStatus == nil || onlineStatus == 0 ? -1 : runStatus","triggerInfoCode":"runStatus"}' where id in ('ACAT0023', 'ACAT0086');


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00005', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'frozenSwitchStatus', '防冻开关状态', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "关闭"}, {"code": "1", "name": "开启"}]', null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00006', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'chillWaterInTemp', '水阀进水温度', null, null, '℃', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00007', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'chillWaterOutTemp', '水阀回水温度', null, null, '℃', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00008', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'chillWaterValveOpening', '水阀水阀开度', null, null, '%', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00009', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'freshAirTemp', '新风温度', null, null, '℃', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00010', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'freshAirRH', '新风相对湿度', null, null, '%', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00011', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'freshAirValveOpening', '新风阀开度', null, null, '%', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00012', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'tTarget', '服务区域目标温度', null, null, '℃', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00013', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'returnAirTemp', '回风温度', null, null, '℃', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00014', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'returnAirRH', '回风相对湿度', null, null, '%RH', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);




REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00015', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'cO2Target', '服务区域目标CO2浓度', null, null, 'ppm', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00016', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'returnAirCO2', '回风CO₂浓度', null, null, 'ppm', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00017', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'returnAirValveOpening', '回风阀开度', null, null, '%', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00018', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'supplyAirTemp', '送风温度', null, null, '℃', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00019', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'supplyAirRH', '送风相对湿度', null, null, '%', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00020', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'supplyFanFreq', '送风机频率', null, null, null, null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00021', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'manualAutoSet', '手自动状态设定', null, null, null, null, 0, null, null, '10', null,
'设定参数', null, null, '[{"code": "1", "name": "AI控制"}, {"code": "0", "name": "手动控制"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00022', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'equipSwitchSet', '设备开关设定', null, null, null, null, 0, null, null, '10', null,
'设定参数', null, null, '[{"code": "0", "name": "关闭"}, {"code": "1", "name": "开启"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00023', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'returnAirTempSet', '回风温度设定', null, null, '℃', null, 1, null, null, '10', null,
'设定参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00024', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'supplyAirTempSet', '送风温度设定', null, null, '℃', null, 1, null, null, '10', null,
'设定参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00025', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'cO2TargetSet', '室内目标CO2浓度设定', null, null, 'ppm', null, 1, null, null, '10', null,
'设定参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00026', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'runModeSet', '运行模式设定', null, null, null, null, 0, null, null, '10', null,
'设定参数', null, null, '[{"code": "1", "name": "供冷"}, {"code": "2", "name": "供热"}]', null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00027', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'regulateMode', '控制调节模式设定', null, null, null, null, 1, null, null, '10', null,
'设定参数', null, null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00028', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'supplyFanFreqSet', '送风机频率设定', null, null, 'Hz', null, 1, null, null, '10', null,
'设定参数', null, null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00029', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'freshAirValveOpeningSet', '新风阀开度设定', null, null, '%', null, 1, null, null, '10', null,
'设定参数', null, null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00030', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'returnAirValveOpeningSet', '回风阀开度设定', null, null, '%', null, 1, null, null, '10', null,
'设定参数', null, null, null, null, null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00031', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'chillWaterValveOpeningSet', '水阀开度设定', null, null, '%', null, 1, null, null, '10', null,
'设定参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00032', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'coolCoilAirTemp', '表冷段后温度', null, null, '℃', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00033', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'mixAirPress', '混风段后压力', null, null, 'Pa', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00034', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'primFilter', '初效过滤器后压力', null, null, 'Pa', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00035', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'medFilter', '中效过滤器后压力', null, null, 'Pa', null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00036', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'supplyAirPress', '送风压力', null, null, null, null, 1, null, null, '10', null,
'运行参数', null, null, null, null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00037', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'frozenAlarm', '防冻报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('ACAT0252', '0', '0', 'info', null, 'ACAT', 'ACATAHZ', '吊装空调', 'supplyFanErrorAlarm', '送风机故障报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', '{"variables":[{"dataType":0,"infoCode":"supplyFanErrorAlarm"}],"sourceIotType":1,"triggerFormula":"supplyFanErrorAlarm != nil && supplyFanErrorAlarm == 1) ? 1 : 0","triggerInfoCode":"faultStatus"}', null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00038', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'supplyFanErrorAlarm', '送风机故障报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', '{"variables":[{"dataType":0,"infoCode":"supplyFanErrorAlarm"}],"sourceIotType":1,"triggerFormula":"supplyFanErrorAlarm != nil && supplyFanErrorAlarm == 1) ? 1 : 0","triggerInfoCode":"faultStatus"}', null);

update rc_info_code_cfg set extend_info = '{"variables":[{"dataType":0,"infoCode":"supplyFanErrorAlarm"}],"sourceIotType":1,"triggerFormula":"supplyFanErrorAlarm != nil && supplyFanErrorAlarm == 1) ? 1 : 0","triggerInfoCode":"faultStatus"}' where id in ('ACAT0057', 'ACAT0252');


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00039', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'fanPressAlarm', '风机送风欠压报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00040', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'primFilterAlarm', '初效过滤器报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', null, null);



REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00041', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'medFilterAlarm', '中效过滤器报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00042', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'freshAirValveOpeningSet', '新风阀状态反馈与指令不符报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00043', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'returnAirValveOpeningSet', '回风阀状态反馈与指令不符报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00044', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'chillWaterValorderFailAlarm', '冷水阀状态反馈与指令不符报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00045', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'supplyFanorderFailAlarm', '送风机状态反馈与指令不符报警', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "报警"}]', null, null);


REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00046', '0', '0', 'info', null, 'ACAT', 'ACATAH', '空调机组', 'faultStatus', '故障状态', null, null, null, null, 0, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "故障"}]', '{"variables":[{"dataType":0,"infoCode":"supplyFanErrorAlarm"}],"sourceIotType":1,"triggerFormula":"supplyFanErrorAlarm != nil && supplyFanErrorAlarm == 1 ? 1 : 0","triggerInfoCode":"faultStatus"}', null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00047', '0', '0', 'info', null, 'ACAT', 'ACATAHG', '组合式空调机组', 'faultStatus', '故障状态', null, null, null, null, null, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "故障"}]', '{"variables":[{"dataType":0,"infoCode":"supplyFanErrorAlarm"}],"sourceIotType":1,"triggerFormula":"supplyFanErrorAlarm != nil && supplyFanErrorAlarm == 1 ? 1 : 0","triggerInfoCode":"faultStatus"}', null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00048', '0', '0', 'info', null, 'ACAT', 'ACATAHZ', '吊装空调', 'faultStatus', '故障状态', null, null, null, null, null, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "故障"}]', '{"variables":[{"dataType":0,"infoCode":"supplyFanErrorAlarm"}],"sourceIotType":1,"triggerFormula":"supplyFanErrorAlarm != nil && supplyFanErrorAlarm == 1 ? 1 : 0","triggerInfoCode":"faultStatus"}', null);

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00049', '0', '0', 'info', null, 'ACAT', 'ACATFU', '新风机组', 'faultStatus', '故障状态', null, null, null, null, null, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "故障"}]', '{"variables":[{"dataType":0,"infoCode":"supplyFanErrorAlarm"},{"dataType":0,"infoCode":"converterErrorAlarm"},{"dataType":0,"infoCode":"elecDustErrorAlarm"}],"sourceIotType":1,"triggerFormula":"supplyFanErrorAlarm != nil && supplyFanErrorAlarm == 1 ? 1 : converterErrorAlarm != nil && converterErrorAlarm == 1 ? 1 : elecDustErrorAlarm != nil && elecDustErrorAlarm == 1 ? 1 : 0","triggerInfoCode":"faultStatus"}', null);

update rc_info_code_cfg set extend_info = '{"variables":[{"dataType":0,"infoCode":"supplyFanErrorAlarm"},{"dataType":0,"infoCode":"converterErrorAlarm"},{"dataType":0,"infoCode":"elecDustErrorAlarm"}],"sourceIotType":1,"triggerFormula":"supplyFanErrorAlarm != nil && supplyFanErrorAlarm == 1 ? 1 : converterErrorAlarm != nil && converterErrorAlarm == 1 ? 1 : elecDustErrorAlarm != nil && elecDustErrorAlarm == 1 ? 1 : 0","triggerInfoCode":"faultStatus"}
' where id in ('ACAT0147', 'ACAT0149', 'ACAT0152');

REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code, class_name, info_code, name, alias, feedback_code, unit, scale,
iot_decimal, max, min, ordered, group_info, parameter_type, display_position, force_info, data_source, extend_info, remark_config)
VALUES ('VIRACAT00050', '0', '0', 'info', null, 'ACAT', 'ACATFC', '风机盘管', 'faultStatus', '故障状态', null, null, null, null, null, null, null, '10', null,
'运行参数', null, null, '[{"code": "0", "name": "正常"}, {"code": "1", "name": "故障"}]', '{"variables":[{"dataType":0,"infoCode":"fanErrorAlarm"}],"sourceIotType":1,"triggerFormula":"fanErrorAlarm != nil && fanErrorAlarm == 1 ? 1 : 0","triggerInfoCode":"faultStatus"}', null);

update rc_info_code_cfg set extend_info = '{"variables":[{"dataType":0,"infoCode":"fanErrorAlarm"}],"sourceIotType":1,"triggerFormula":"fanErrorAlarm != nil && fanErrorAlarm == 1 ? 1 : 0","triggerInfoCode":"faultStatus"}' where id in ('ACAT0178');
