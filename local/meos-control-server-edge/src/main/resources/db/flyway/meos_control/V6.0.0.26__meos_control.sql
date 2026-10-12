CREATE TABLE IF NOT EXISTS `class_position_code_config` (
    `id` varchar(200) NOT NULL COMMENT '主键ID',
    `group_code` varchar(100) DEFAULT NULL COMMENT '集团编码',
    `project_id` varchar(50) DEFAULT NULL COMMENT '项目id',
    `class_code` varchar(255) NOT NULL COMMENT '中台的classCode',
    `position_code` varchar(255) DEFAULT NULL COMMENT '部件编码',
    `position_name` varchar(255) DEFAULT NULL COMMENT '部件名称',
    `info_code` varchar(255) DEFAULT NULL COMMENT '信息点编码',
    `create_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `nk_position_code_project_id` (`project_id`) USING BTREE COMMENT '项目级别索引',
    KEY `nk_position_code_class_code` (`class_code`) USING BTREE COMMENT '设备类级别索引'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT = '设备半剖图展示信息点配置';


REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000chillWaterInTemp', '0', '0', '0', null, '加热段（热盘管）', 'chillWaterInTemp');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000chillWaterOutTemp', '0', '0', '0', null, '加热段（热盘管）', 'chillWaterOutTemp');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000chillWaterValveOpening', '0', '0', '0', null, '加热段（热盘管）', 'chillWaterValveOpening');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000chillWaterValveOpeningSet', '0', '0', '0', null, '加热段（热盘管）', 'chillWaterValveOpeningSet');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000coolCoilAirTemp', '0', '0', '0', null, '表冷段（冷盘管）', 'coolCoilAirTemp');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000freshAirRH', '0', '0', '0', null, '新风段', 'freshAirRH');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000freshAirTemp', '0', '0', '0', null, '新风段', 'freshAirTemp');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000freshAirValveOpening', '0', '0', '0', null, '新风段', 'freshAirValveOpening');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000freshAirValveOpeningSet', '0', '0', '0', null, '新风段', 'freshAirValveOpeningSet');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000frozenAlarm', '0', '0', '0', null, '表冷段（冷盘管）', 'frozenAlarm');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000medFilter', '0', '0', '0', null, '中效过滤段', 'medFilter');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000medFilterAlarm', '0', '0', '0', null, '中效过滤段', 'medFilterAlarm');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000mixAirPress', '0', '0', '0', null, '混风段', 'mixAirPress');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000primFilter', '0', '0', '0', null, '初效过滤段', 'primFilter');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000primFilterAlarm', '0', '0', '0', null, '初效过滤段', 'primFilterAlarm');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000returnAirCO2', '0', '0', '0', null, '回风管', 'returnAirCO2');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000returnAirRH', '0', '0', '0', null, '回风管', 'returnAirRH');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000returnAirTemp', '0', '0', '0', null, '回风管', 'returnAirTemp');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000returnAirValveOpening', '0', '0', '0', null, '回风管', 'returnAirValveOpening');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000returnAirValveOpeningSet', '0', '0', '0', null, '回风管', 'returnAirValveOpeningSet');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000supplyAirPress', '0', '0', '0', null, '送风管', 'supplyAirPress');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000supplyAirRH', '0', '0', '0', null, '送风管', 'supplyAirRH');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000supplyAirTemp', '0', '0', '0', null, '送风管', 'supplyAirTemp');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000supplyFanErrorAlarm', '0', '0', '0', null, '风机', 'supplyFanErrorAlarm');
REPLACE INTO class_position_code_config (id, group_code, project_id, class_code, position_code, position_name, info_code) VALUES ('000supplyFanFreqSet', '0', '0', '0', null, '风机', 'supplyFanFreqSet');



REPLACE INTO rc_info_code_cfg (id, group_code, project_id, data_type, system_id, system_type, class_code,
                               class_name, info_code, name, alias, feedback_code, unit, scale, iot_decimal,
                               max, min, ordered, group_info, parameter_type, display_position, force_info,
                               data_source, extend_info, remark_config)
VALUES ('ACCI00023', '0', '0', 'info', null, 'ACCI', 'ACCI', '中央冷热源系统', 'runModeSet', '运行模式',
        null, null, null, null, 0, null, null, '20', null, '设定参数', null, null, '[{"code": "1", "name": "制冷"}, {"code": "2", "name": "制热"}, {"code": "3", "name": "通风"}, {"code": "4", "name": "除湿"}]', null, null);


update rc_info_code_cfg set extend_info = '{"variables":[{"dataType":0,"infoCode":"chillWaterMainReturnTemp"},{"dataType":0,"infoCode":"chillWaterMainSupplyTemp"}],"sourceIotType":1,"triggerFormula":"(chillWaterMainReturnTemp == nil || chillWaterMainSupplyTemp == nil) ? nil : (chillWaterMainReturnTemp - chillWaterMainSupplyTemp)","triggerInfoCode":"chillWaterSupplyReturnTemp"}' where id = 'ACCIIF008';
update rc_info_code_cfg set extend_info = '{"variables":[{"dataType":0,"infoCode":"chillWaterMainReturnPress"},{"dataType":0,"infoCode":"chillWaterMainSupplyPress"}],"sourceIotType":1,"triggerFormula":"(chillWaterMainReturnPress == nil || chillWaterMainSupplyPress == nil) ? nil : (chillWaterMainReturnPress - chillWaterMainSupplyPress)","triggerInfoCode":"chillWaterSupplyReturnPress"}' where id = 'ACCIIF009';

update rc_info_code_cfg set group_info = null where id = 'ACCIIF016';
update rc_info_code_cfg set group_info = null where id = 'ACCIIF017';


update rc_basics_config set result = '{"default":[{"templateId":"9725780e-831e-47ab-8b7c-6980d2fcd72c","templateName":"主系统图","order":1,"legendScaleRatio":2}],"water":[{"templateId":"ec3263a1-32ef-4777-82cf-35a5ec8e9dc5","templateName":"","order":1,"legendScaleRatio":2}]}' where id = 'ACCISYSTEMTEMPLATED';