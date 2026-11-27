# ------------------------- equip_record 补索引 -------------------------
# 原因：4.91 上 `WHERE group_code = ? AND modified_time >= ? AND modified_time < ?` 的查询
# 有 54% 未用索引（67 小时 4,058 次）。现有索引都不含 modified_time。
call add_table_index('equip_record', 'idx_group_code_modified_time',
                     'create index idx_group_code_modified_time
    on equip_record (group_code, modified_time);');
