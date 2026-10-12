REPLACE INTO rc_basics_config (id, project_id, type, child_type, st, result, create_user, create_time, update_user, update_time)
VALUES ('ACATSYSTEMTEMPLATED', null, 'ACAT', null, 'valid',
'{"default":[{"templateId":"769f8409-49df-4ae5-bd5b-fb498364ee1e","templateName":"冷冻水供水视图","order":1,"legendScaleRatio":2},{"templateId":"ec3263a1-32ef-4777-82cf-35a5ec8e9dc4","templateName":"冷冻水回水视图","order":2,"legendScaleRatio":2}],"water":[{"templateId":"bd5f6282-6621-46d7-913b-3009823870a4","templateName":"","order":1,"legendScaleRatio":2},{"templateId":"98451f84-9c89-4904-9691-eed0119c010e","templateName":"","order":2,"legendScaleRatio":2},{"templateId":"77b41346-6bcf-408b-ba3b-b07a55a41d48","templateName":"","order":3,"legendScaleRatio":2},{"templateId":"84776b33-23a7-40ba-9864-7e18f093ae7d","templateName":"","order":4,"legendScaleRatio":2}]}',
'systemId', '2023-12-28 11:48:38', 'systemId', '2024-01-15 17:40:52');


update rc_info_code_cfg set data_source = '[{"code": "1", "name": "AI控制"}, {"code": "0", "name": "手动控制"}]' where id = 'ACAT0040';
update rc_info_code_cfg set data_source = '[{"code": "1", "name": "AI控制"}, {"code": "0", "name": "手动控制"}]' where id = 'ACAT0093';
update rc_info_code_cfg set data_source = '[{"code": "1", "name": "AI控制"}, {"code": "0", "name": "手动控制"}]' where id = 'ACAT0138';
update rc_info_code_cfg set data_source = '[{"code": "1", "name": "AI控制"}, {"code": "0", "name": "手动控制"}]' where id = 'ACAT0172';


update rc_info_code_cfg set data_source = '[{"code": "1", "name": "卧室"}, {"code": "2", "name": "吊装"}]' where id = 'ACAT0006';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0007';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0008';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0009';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0010';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0011';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0012';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0013';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0014';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0015';
update rc_info_code_cfg set data_source = '[{"code": "1", "name": "无热回收"}, {"code": "2", "name": "板式"}, {"code": "3", "name": "转轮式"}, {"code": "4", "name": "热管式"}, {"code": "5","name": "中间热媒式"}, {"code": "6", "name": "热泵式"}, {"code": "99", "name": "其他"}]' where id = 'ACAT0016';
update rc_info_code_cfg set data_source = '[{"code": "1", "name": "卧室"}, {"code": "2", "name": "吊装"}]' where id = 'ACAT0071';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0072';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0073';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0074';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0075';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0076';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0077';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0078';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0079';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0080';
update rc_info_code_cfg set data_source = '[{"code": "1", "name": "无热回收"}, {"code": "2", "name": "板式"}, {"code": "3", "name": "转轮式"}, {"code": "4", "name": "热管式"}, {"code": "5", "name": "中间热媒式"}, {"code": "6", "name": "热泵式"}, {"code": "99", "name": "其他"}]' where id = 'ACAT0081';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0112';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0113';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0114';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0115';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0116';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0117';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0118';
update rc_info_code_cfg set data_source = '[{"code": "1", "name": "无热回收"}, {"code": "2", "name": "板式"}, {"code": "3", "name": "转轮式"}, {"code": "4", "name": "热管式"}, {"code": "5", "name": "中间热媒式"}, {"code": "6", "name": "热泵式"}, {"code": "99", "name": "其他"}]' where id = 'ACAT0119';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0162';
update rc_info_code_cfg set data_source = '[{"code": "0", "name": "无"}, {"code": "1", "name": "有"}]' where id = 'ACAT0163';


update rc_info_code_cfg set iot_decimal = 0 where data_source is not null and iot_decimal is null;
UPDATE rc_info_code_cfg SET iot_decimal = 1 WHERE iot_decimal is null and parameter_type in ('运行参数', '设定参数');