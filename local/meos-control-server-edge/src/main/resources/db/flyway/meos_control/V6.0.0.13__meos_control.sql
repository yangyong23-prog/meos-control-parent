UPDATE rc_info_code_cfg SET parameter_type = '基础参数' WHERE id = 'IF000108';
UPDATE rc_info_code_cfg SET iot_decimal = 1 WHERE iot_decimal is null and parameter_type = '运行参数';
UPDATE rc_info_code_cfg SET display_position = '["details"]' WHERE class_code in ('ACGECL', 'ACGEDB') and info_code = 'localId';
UPDATE rc_info_code_cfg SET display_position = '["home_page", "details"]' WHERE class_code in ('ACGECL', 'ACGEDB') and info_code = 'localName';
UPDATE rc_info_code_cfg SET ordered = '2' WHERE id = 'IF000009';
UPDATE rc_info_code_cfg SET ordered = '1' WHERE id = 'IF000010';
UPDATE rc_info_code_cfg SET ordered = '3' WHERE id = 'GPCC001';
UPDATE rc_info_code_cfg SET ordered = '2' WHERE id = 'GPLQ001';
UPDATE rc_info_code_cfg SET ordered = '1' WHERE id = 'GPLD001';