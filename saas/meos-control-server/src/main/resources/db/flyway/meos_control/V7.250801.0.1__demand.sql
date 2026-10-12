# ------------------------- 需求场景表 -------------------------
call drop_table_column('demand_scene', 'across_day');
call drop_table_column('demand_scene', 'across_day');
call drop_table_column('demand_scene_snapshot', 'across_day');

call add_table_column('demand_scene', 'sequence',
                      'alter table demand_scene add sequence int default 0 not null comment ''排序'' after end_time;');
call add_table_column('demand_scene', 'rel_template_id',
                      'alter table demand_scene add rel_template_id bigint not null comment ''关联需求模版ID'' after sequence;');
call add_table_column('demand_scene_snapshot', 'sequence',
                      'alter table demand_scene_snapshot add sequence int default 0 not null comment ''排序'' after end_time;');
call add_table_column('demand_scene_snapshot', 'rel_template_id',
                      'alter table demand_scene_snapshot add rel_template_id bigint not null comment ''关联需求模版ID'' after sequence;');

call drop_table_index('demand_scene', 'demand_scene_idx');
call add_table_index('demand_scene', 'demand_scene_idx',
                     'create index demand_scene_idx on demand_scene (project_id, rel_template_id, scene_name);');
call drop_table_index('demand_scene_snapshot', 'demand_scene_snapshot_idx');
call add_table_index('demand_scene_snapshot', 'demand_scene_snapshot_idx',
                     'create index demand_scene_snapshot_idx on demand_scene_snapshot (project_id, rel_template_id, update_time, valid_end_time, scene_name);');

# ------------------------- 需求配置表 -------------------------
call drop_table_index('demand_config', 'demand_config_group_pk');
call add_table_index('demand_config', 'demand_config_group_idx',
                     'create index demand_config_group_idx on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id, rel_group_id);');
call drop_table_index('demand_config', 'demand_config_pk');
call add_table_index('demand_config', 'demand_config_idx',
                     'create index demand_config_idx on demand_config (project_id, demand_instance_type, demand_instance_id, rel_template_Id);');

# ------------------------- create trigger demand_scene_insert_snapshot -------------------------
drop trigger if exists demand_scene_insert_snapshot;
delimiter $$
create trigger demand_scene_insert_snapshot
    after insert
    on demand_scene
    for each row
begin
    insert into demand_scene_snapshot (id, project_id, demand_template_type, demand_template_Tag, scene_name,
                                       begin_time, end_time, sequence, rel_template_id, create_time,
                                       create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_template_type, NEW.demand_template_Tag, NEW.scene_name,
               NEW.begin_time, NEW.end_time, NEW.sequence, NEW.rel_template_id, NEW.create_time,
               NEW.create_user, NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# -------------------- create trigger demand_scene_update_snapshot --------------------
drop trigger if exists demand_scene_update_snapshot;
delimiter $$
create trigger demand_scene_update_snapshot
    after update
    on demand_scene
    for each row
begin
    update demand_scene_snapshot
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_scene_snapshot (id, project_id, demand_template_type, demand_template_Tag, scene_name,
                                       begin_time, end_time, sequence, rel_template_id, create_time,
                                       create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_template_type, NEW.demand_template_Tag, NEW.scene_name,
               NEW.begin_time, NEW.end_time, NEW.sequence, NEW.rel_template_id, NEW.create_time,
               NEW.create_user, NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# -------------------- create trigger demand_scene_delete_snapshot --------------------
drop trigger if exists demand_scene_delete_snapshot;
delimiter $$
create trigger demand_scene_delete_snapshot
    after delete
    on demand_scene
    for each row
begin
    update demand_scene_snapshot
    set valid_end_time = current_timestamp(6)
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;