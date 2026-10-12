call add_table_column('system_graph_template_status', 'name',
                      'alter table system_graph_template_status add name varchar(500) DEFAULT NULL COMMENT ''模版名称'' after `object_id`;');

call add_table_column('system_graph_template_status', 'alias_name',
                      'alter table system_graph_template_status add alias_name varchar(500) DEFAULT NULL COMMENT ''模版别名'' after `name`;');