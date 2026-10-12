-- 修改 system_graph_duplicate 表，云，边
call Pro_Temp_ColumnWork('system_graph_duplicate','template_id',' varchar(64) DEFAULT NULL COMMENT ''模板ID''', 2);

-- 删除多余原始图纸的系统图数据
delete from system_graph_data where id in(
    SELECT system_graph_data_id FROM system_graph_duplicate
    WHERE graph_type = 0
      AND (project_id, system_object_id, graph_type_code, update_time) NOT IN (
        SELECT project_id, system_object_id, graph_type_code, MAX(update_time)
        FROM system_graph_duplicate
        WHERE graph_type = 0
        GROUP BY project_id, system_object_id, graph_type_code
    ));

-- 删除系统图副本表多余的原始图纸
DELETE FROM system_graph_duplicate
WHERE graph_type = 0
  AND (project_id, system_object_id, graph_type_code, update_time) NOT IN (
    SELECT * FROM (
                      SELECT project_id, system_object_id, graph_type_code, MAX(update_time)
                      FROM system_graph_duplicate
                      WHERE graph_type = 0
                      GROUP BY project_id, system_object_id, graph_type_code
                  ) AS temp
);


