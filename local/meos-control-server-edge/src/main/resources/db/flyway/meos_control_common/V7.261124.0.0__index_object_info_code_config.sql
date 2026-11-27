# ------------------------- object_info_code_config 补索引 -------------------------
# 原因：4.91 上 `WHERE info_code_type IN (...) AND rel_template_type_id IN (...)` 及
# `WHERE rel_template_type_id IN (...)` 大多走全表扫描（67 小时 5,137 次，每次约 7.5 万行）。
# 现有索引 object_info_code_config_idx 以 info_code_type 打头，优化器多数情况不用它；
# 以选择性更高的 rel_template_type_id 打头可同时覆盖两种查询。
call add_table_index('object_info_code_config', 'idx_rel_template_type_info_code_type',
                     'create index idx_rel_template_type_info_code_type
    on object_info_code_config (rel_template_type_id, info_code_type);');
