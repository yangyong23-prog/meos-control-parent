# ------------------------- object_info_code_template_type -------------------------
# 新增对象类导入顺序
call add_table_column('object_info_code_template_type', 'sequence',
                      'alter table `object_info_code_template_type` add `sequence` int NULL COMMENT ''设备类顺序'' after `info_code_filter`;');
