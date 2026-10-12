-- ACCCACequipSwitchSet 支持开启中的计算配置
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
WHERE t.id = 'ACCCACequipSwitchSet';