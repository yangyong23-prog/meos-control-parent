-- 修改 system_graph_duplicate 表，云，边
call Pro_Temp_ColumnWork('system_graph_duplicate','template_id',' varchar(64) DEFAULT NULL COMMENT ''模板ID''', 2);


-- 修改 system_graph_template_status 表 云
call Pro_Temp_ColumnWork('system_graph_template_status','template_id',' varchar(64) DEFAULT NULL COMMENT ''模板ID''', 2);


-- 修改 system_graph_async_job 表 云
call Pro_Temp_ColumnWork('system_graph_async_job','template_ori_Id',' varchar(64) DEFAULT NULL COMMENT ''模板ID''', 2);

-- 添加图类型和顺序号
call Pro_Temp_ColumnWork('system_graph_template_status','sequence_number_current',' int DEFAULT NULL COMMENT ''顺序号'' after template_id', 1);
call Pro_Temp_ColumnWork('system_graph_template_status','graph_type_code',' varchar(100) DEFAULT NULL COMMENT ''图类型'' after template_id', 1);

-- 删除多余原始图纸的系统图数据
delete from system_graph_data where id in(
    SELECT system_graph_data_id FROM system_graph_duplicate
    WHERE graph_type = 0
      AND (project_id, system_object_id, graph_type_code, update_time) NOT IN (
        SELECT project_id,system_object_id, graph_type_code, MAX(update_time)
        FROM system_graph_duplicate
        WHERE graph_type = 0
        GROUP BY project_id,system_object_id, graph_type_code
    ));

-- 删除系统图副本表多余的原始图纸
DELETE FROM system_graph_duplicate
WHERE graph_type = 0
  AND (project_id,system_object_id, graph_type_code, update_time) NOT IN (
    SELECT * FROM (
                      SELECT project_id,system_object_id, graph_type_code, MAX(update_time)
                      FROM system_graph_duplicate
                      WHERE graph_type = 0
                      GROUP BY project_id,system_object_id, graph_type_code
                  ) AS temp
);

-- 清理模板状态表
delete from system_graph_template_status;

