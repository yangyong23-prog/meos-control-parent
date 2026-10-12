INSERT IGNORE INTO rc_info_code_cfg (id, data_type, system_id, system_type, class_code, class_name, info_code, name,
                                     alias, feedback_code, unit, scale, iot_decimal, max, min, ordered, group_info,
                                     parameter_type, display_position, force_info, data_source, extend_info)
VALUES ('IF000197', 'info', null, 'ACCC', 'ACAT', '空调末端系统', 'localName', '系统本地名称', null, null, null, null,
        null, null, null, '1', 'GPLZJC001', '基础参数', '["home_page", "details"]', null, null, null);


CALL Pro_Temp_ColumnWork('strategy_date_cfg', 'update_user',
                         '  varchar(64) null comment ''修改人''',
                         1);

INSERT IGNORE INTO `rc_basics_async_task` (`id`, `type`, `sys`, `task_st`, `execute_st`, `time_frequency`, `memo`,
                                    `create_time`, `update_time`, `syn_time`)
SELECT 'alarm_stale',
       'alarm_stale',
       'locality',
       'valid',
       'init',
       300,
       '报警过期任务',
       now(),
       now(),
       now()
FROM DUAL
WHERE NOT EXISTS(SELECT 1 FROM `rc_basics_async_task` WHERE `type` = 'alarm_stale');


UPDATE rc_info_code_cfg SET extend_info = '{"variables": [{"dataType": 0, "infoCode": "onlineStatus"}, {"dataType": 1, "infoCode": "enableCtrl"}, {"dataType": 0, "infoCode": "runStatus"}], "sourceIotType": 0, "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (enableCtrl == nil ? (runStatus == nil ? -1 : runStatus) : (enableCtrl == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))", "triggerInfoCode": "rcRunStatus"}' WHERE id = 'IF000060';
UPDATE rc_info_code_cfg SET extend_info = '{"variables": [{"dataType": 0, "infoCode": "onlineStatus"}, {"dataType": 1, "infoCode": "equipSwitchSet"}, {"dataType": 0, "infoCode": "runStatus"}], "sourceIotType": 0, "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (equipSwitchSet == nil ? (runStatus == nil ? -1 : runStatus) : (equipSwitchSet == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))", "triggerInfoCode": "rcRunStatus"}' WHERE id = 'IF000062';
UPDATE rc_info_code_cfg SET extend_info = '{"variables": [{"dataType": 0, "infoCode": "onlineStatus"}, {"dataType": 1, "infoCode": "equipSwitchSet"}, {"dataType": 0, "infoCode": "runStatus"}], "sourceIotType": 0, "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (equipSwitchSet == nil ? (runStatus == nil ? -1 : runStatus) : (equipSwitchSet == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))", "triggerInfoCode": "rcRunStatus"}' WHERE id = 'IF000082';
UPDATE rc_info_code_cfg SET extend_info = '{"variables": [{"dataType": 0, "infoCode": "onlineStatus"}, {"dataType": 1, "infoCode": "equipSwitchSet"}, {"dataType": 0, "infoCode": "runStatus"}], "sourceIotType": 0, "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (equipSwitchSet == nil ? (runStatus == nil ? -1 : runStatus) : (equipSwitchSet == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))", "triggerInfoCode": "rcRunStatus"}' WHERE id = 'IF000101';
UPDATE rc_info_code_cfg SET extend_info = '{"variables": [{"dataType": 1, "infoCode": "enableCtrl"}, {"dataType": 0, "infoCode": "runStatus"}], "sourceIotType": 0, "triggerFormula": "enableCtrl == nil ? runStatus == nil ? -1 : runStatus : enableCtrl == 0 ? runStatus == nil ? enableCtrl : runStatus == 0 ? 0 : 3 : runStatus == nil ? enableCtrl : runStatus == 1 ? 1 : 2", "triggerInfoCode": "rcRunStatus"}' WHERE id = 'IF000107';
UPDATE rc_info_code_cfg SET extend_info = '{"variables": [{"dataType": 0, "infoCode": "onlineStatus"}, {"dataType": 1, "infoCode": "equipSwitchSet"}, {"dataType": 0, "infoCode": "runStatus"}], "sourceIotType": 0, "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (equipSwitchSet == nil ? (runStatus == nil ? -1 : runStatus) : (equipSwitchSet == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))", "triggerInfoCode": "rcRunStatus"}' WHERE id = 'IF000115';
