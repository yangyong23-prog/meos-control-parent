--  支持开启中的计算配置
UPDATE `info_code_config` t
SET t.extend_info = '{
  "view": "1",
  "triggerFormulaInfo": {
    "variables": [
      {
        "dataType": 0,
        "infoCode": "onlineStatus"
      },
      {
        "dataType": 1,
        "infoCode": "equipSwitchSet"
      },
      {
        "dataType": 0,
        "infoCode": "runStatus"
      }
    ],
    "sourceIotType": 0,
    "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (equipSwitchSet == nil ? (runStatus == nil ? -1 : runStatus) : (equipSwitchSet == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
    "triggerInfoCode": "rcRunStatus"
  }
}'
WHERE t.id in ('ACAHCequipSwitchSet',
               'ACCCACequipSwitchSet',
               'ACCCGHequipSwitchSet',
               'ACCCWHequipSwitchSet',
               'ACCHCBequipSwitchSet',
               'ACCHCHequipSwitchSet',
               'ACCHEBequipSwitchSet',
               'ACCHFBequipSwitchSet',
               'ACCHGBequipSwitchSet',
               'ACCHHPequipSwitchSet');

--  支持开启中的计算配置
UPDATE `info_code_config` t
SET t.extend_info = '{
  "view": "1",
  "control": true,
  "triggerFormulaInfo": {
    "variables": [
      {
        "dataType": 0,
        "infoCode": "onlineStatus"
      },
      {
        "dataType": 1,
        "infoCode": "equipSwitchSet"
      },
      {
        "dataType": 0,
        "infoCode": "runStatus"
      }
    ],
    "sourceIotType": 0,
    "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (equipSwitchSet == nil ? (runStatus == nil ? -1 : runStatus) : (equipSwitchSet == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
    "triggerInfoCode": "rcRunStatus"
  }
}'
WHERE t.id in ('CONTROLACAHCequipSwitchSet',
               'CONTROLACCHCHequipSwitchSet',
               'CONTROLACCHGBequipSwitchSet',
               'CONTROLACCHHPequipSwitchSet');

-- enableCtrl
UPDATE `info_code_config` t
SET t.extend_info = '{
  "view": "1",
  "forceInfoCodeInfos": [
    {
      "infoCode": "equipSwitchSet",
      "classCode": "ACAHC"
    },
    {
      "infoCode": "indoorMediumValveSwitchSet",
      "classCode": "ACAHC"
    }
  ],
  "triggerFormulaInfo": {
    "variables": [
      {
        "dataType": 0,
        "infoCode": "onlineStatus"
      },
      {
        "dataType": 1,
        "infoCode": "enableCtrl"
      },
      {
        "dataType": 0,
        "infoCode": "runStatus"
      }
    ],
    "sourceIotType": 0,
    "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (enableCtrl == nil ? (runStatus == nil ? -1 : runStatus) : (enableCtrl == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
    "triggerInfoCode": "rcRunStatus"
  }
}'
WHERE t.id in ('ACAHCenableCtrl');

UPDATE `info_code_config` t
SET t.extend_info = '{
  "view": "1",
  "triggerFormulaInfo": {
    "variables": [
      {
        "dataType": 0,
        "infoCode": "onlineStatus"
      },
      {
        "dataType": 1,
        "infoCode": "enableCtrl"
      },
      {
        "dataType": 0,
        "infoCode": "runStatus"
      }
    ],
    "sourceIotType": 0,
    "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (enableCtrl == nil ? (runStatus == nil ? -1 : runStatus) : (enableCtrl == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
    "triggerInfoCode": "rcRunStatus"
  }
}'
WHERE t.id in ('ACCHGBenableCtrl');


UPDATE `info_code_config` t
SET t.extend_info = '{
  "view": "1",
  "control": true,
  "triggerFormulaInfo": {
    "variables": [
      {
        "dataType": 0,
        "infoCode": "onlineStatus"
      },
      {
        "dataType": 1,
        "infoCode": "enableCtrl"
      },
      {
        "dataType": 0,
        "infoCode": "runStatus"
      }
    ],
    "sourceIotType": 0,
    "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (enableCtrl == nil ? (runStatus == nil ? -1 : runStatus) : (enableCtrl == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
    "triggerInfoCode": "rcRunStatus"
  }
}'
WHERE t.id in ('CONTROLACAHCenableCtrl');

-- 新增设备类
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                 class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                 obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                 extend_info, create_time, update_time)
VALUES ('ACCCWHenableCtrlCtrl', null, null, 'enableCtrl', '启用命令', null, null, 'ACCCWH', '水源热泵', null, null, 'GPSD001',
        null, null, null, null, '设定参数', null, '8', null,
        '{
          "view": "1",
          "control": true,
          "forceInfoCodeInfos": [
            {
              "infoCode": "equipSwitchSet",
              "classCode": "ACCCWH"
            },
            {
              "infoCode": "indoorMediumValveSwitchSet",
              "classCode": "ACCCWH"
            }
          ],
          "triggerFormulaInfo": {
            "variables": [
              {
                "dataType": 0,
                "infoCode": "onlineStatus"
              },
              {
                "dataType": 1,
                "infoCode": "enableCtrl"
              },
              {
                "dataType": 0,
                "infoCode": "runStatus"
              }
            ],
            "sourceIotType": 0,
            "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (enableCtrl == nil ? (runStatus == nil ? -1 : runStatus) : (enableCtrl == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
            "triggerInfoCode": "rcRunStatus"
          }
        }',
        now(), now());


REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                 class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                 obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                 extend_info, create_time, update_time)
VALUES ('ACCCGHenableCtrlCtrl', null, null, 'enableCtrl', '启用命令', null, null, 'ACCCGH', '地源热泵', null, null, 'GPSD001',
        null, null, null, null, '设定参数', null, '8', null,
        '{
          "view": "1",
          "control": true,
          "forceInfoCodeInfos": [
            {
              "infoCode": "equipSwitchSet",
              "classCode": "ACCCGH"
            },
            {
              "infoCode": "indoorMediumValveSwitchSet",
              "classCode": "ACCCGH"
            }
          ],
          "triggerFormulaInfo": {
            "variables": [
              {
                "dataType": 0,
                "infoCode": "onlineStatus"
              },
              {
                "dataType": 1,
                "infoCode": "enableCtrl"
              },
              {
                "dataType": 0,
                "infoCode": "runStatus"
              }
            ],
            "sourceIotType": 0,
            "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (enableCtrl == nil ? (runStatus == nil ? -1 : runStatus) : (enableCtrl == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
            "triggerInfoCode": "rcRunStatus"
          }
        }',
        now(), now());


REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                 class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                 obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                 extend_info, create_time, update_time)
VALUES ('ACCCACenableCtrlCtrl', null, null, 'enableCtrl', '启用命令', null, null, 'ACCCAC', '吸收式冷水机组', null, null, 'GPSD001',
        null, null, null, null, '设定参数', null, '8', null,
        '{
          "view": "1",
          "control": true,
          "forceInfoCodeInfos": [
            {
              "infoCode": "equipSwitchSet",
              "classCode": "ACCCAC"
            },
            {
              "infoCode": "indoorMediumValveSwitchSet",
              "classCode": "ACCCAC"
            }
          ],
          "triggerFormulaInfo": {
            "variables": [
              {
                "dataType": 0,
                "infoCode": "onlineStatus"
              },
              {
                "dataType": 1,
                "infoCode": "enableCtrl"
              },
              {
                "dataType": 0,
                "infoCode": "runStatus"
              }
            ],
            "sourceIotType": 0,
            "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (enableCtrl == nil ? (runStatus == nil ? -1 : runStatus) : (enableCtrl == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
            "triggerInfoCode": "rcRunStatus"
          }
        }',
        now(), now());



REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                 class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                 obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                 extend_info, create_time, update_time)
VALUES ('ACCHHPenableCtrlCtrl', null, null, 'enableCtrl', '启用命令', null, null, 'ACCHHP', '供热水泵', null, null, 'GPSD001',
        null, null, null, null, '设定参数', null, '8', null,
        '{
          "view": "1",
          "control": true,
          "forceInfoCodeInfos": [
            {
              "infoCode": "equipSwitchSet",
              "classCode": "ACCHHP"
            },
            {
              "infoCode": "indoorMediumValveSwitchSet",
              "classCode": "ACCHHP"
            }
          ],
          "triggerFormulaInfo": {
            "variables": [
              {
                "dataType": 0,
                "infoCode": "onlineStatus"
              },
              {
                "dataType": 1,
                "infoCode": "enableCtrl"
              },
              {
                "dataType": 0,
                "infoCode": "runStatus"
              }
            ],
            "sourceIotType": 0,
            "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (enableCtrl == nil ? (runStatus == nil ? -1 : runStatus) : (enableCtrl == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
            "triggerInfoCode": "rcRunStatus"
          }
        }',
        now(), now());



REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                 class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                 obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                 extend_info, create_time, update_time)
VALUES ('ACCHCHenableCtrlCtrl', null, null, 'enableCtrl', '启用命令', null, null, 'ACCHCH', '冷热水循环水泵', null, null, 'GPSD001',
        null, null, null, null, '设定参数', null, '8', null,
        '{
          "view": "1",
          "control": true,
          "forceInfoCodeInfos": [
            {
              "infoCode": "equipSwitchSet",
              "classCode": "ACCHCH"
            },
            {
              "infoCode": "indoorMediumValveSwitchSet",
              "classCode": "ACCHCH"
            }
          ],
          "triggerFormulaInfo": {
            "variables": [
              {
                "dataType": 0,
                "infoCode": "onlineStatus"
              },
              {
                "dataType": 1,
                "infoCode": "enableCtrl"
              },
              {
                "dataType": 0,
                "infoCode": "runStatus"
              }
            ],
            "sourceIotType": 0,
            "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (enableCtrl == nil ? (runStatus == nil ? -1 : runStatus) : (enableCtrl == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
            "triggerInfoCode": "rcRunStatus"
          }
        }',
        now(), now());

REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000081', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACCHEB', '电锅炉', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000082', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACCHCB', '燃煤锅炉', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000083', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACCHGB', '燃气锅炉', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000084', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACCHFB', '燃油锅炉', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000085', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACAHC', '空气源热泵控制组', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000086', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACCCWH', '水源热泵', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000087', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACCCGH', '地源热泵', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000088', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACCCAC', '吸收式冷水机组', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000089', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACCHHP', '供热水泵', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());
REPLACE INTO `info_code_config` (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
                                class_code, class_name, merge_code, merge_name, group_info, unit, scale,
                                obj_type, data_type, first_tag, second_tag, ordered, data_source,
                                extend_info, create_time, update_time)
VALUES ('VIR000090', null, null, 'rcRunStatus', '设备运行状态', null, null, 'ACCHCH', '冷热水循环水泵', null, null, null,
        null, null, null, null, '运行参数', null, '1',
        '[{"code": "-1", "name": "无效"}, {"code": "0", "name": "停止"}, {"code": "1", "name": "运行"}, {"code": "2", "name": "开启中"}, {"code": "3", "name": "关闭中"}]',
        null, now(), now());


