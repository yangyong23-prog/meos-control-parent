# ------------------------- 日志记录ID 更改为 UUID  -------------------------
call Pro_Temp_ColumnWork('rc_running_log', 'id', ' varchar(100) COMMENT "主键ID"', 2);
