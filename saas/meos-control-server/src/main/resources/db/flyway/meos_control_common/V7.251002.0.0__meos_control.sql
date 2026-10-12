--  支持开启中的计算配置
UPDATE meos_control.info_code_config t
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
UPDATE meos_control.info_code_config t
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
UPDATE meos_control.info_code_config t
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

UPDATE meos_control.info_code_config t
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


UPDATE meos_control.info_code_config t
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
REPLACE INTO meos_control.info_code_config (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
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


REPLACE INTO meos_control.info_code_config (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
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


REPLACE INTO meos_control.info_code_config (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
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



REPLACE INTO meos_control.info_code_config (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
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



REPLACE INTO meos_control.info_code_config (id, group_code, project_id, info_code, name, alias_name, abbreviation_name,
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