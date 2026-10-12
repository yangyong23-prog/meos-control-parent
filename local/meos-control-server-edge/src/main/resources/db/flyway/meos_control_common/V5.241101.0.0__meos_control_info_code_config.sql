# ------------------------- object_info_code_config -------------------------
# 新增信息点别名字段
call add_table_column('object_info_code_config', 'info_code_alias_name',
                      'alter table `object_info_code_config` add `info_code_alias_name` varchar(64) null comment ''信息点别名'' after `info_code_name`;');
# 修改信息点类型字段长度
call edit_table_column('object_info_code_config', 'info_code_type',
                       'alter table `object_info_code_config` modify `info_code_type` varchar(64) not null comment ''信息点类型'';');
# 清空配置表和模版表无关联的数据
delete
from object_info_code_config oicc
where not exists (select 1 from object_info_code_template_type oictt where oicc.rel_template_type_id = oictt.id);