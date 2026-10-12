# -------------------- create function parse_project_timestamp --------------------
drop function if exists parse_project_timestamp;
delimiter $$
create function parse_project_timestamp(project_id varchar(64)) returns varchar(64)
begin
    declare now_time timestamp(6) default current_timestamp(6);
    declare project_time_zone varchar(64);
    select config
    into project_time_zone
    from meos_control.common_config
    where tag = binary concat('TIME_ZONE-', project_id);
    if project_time_zone is not null then
        set now_time = convert_tz(now_time, (select @@session.time_zone), project_time_zone);
    end if;
    return now_time;
end $$
delimiter ;

# -------------------- create trigger demand_config_update_snapshot --------------------
drop trigger if exists demand_config_update_snapshot;
delimiter $$
create trigger demand_config_update_snapshot
    after update
    on demand_config
    for each row
begin
    update demand_config_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_config_snapshot (id, project_id, demand_instance_type, demand_instance_id, config_data,
                                        rel_template_Id, rel_scene_id, rel_group_id, create_time, create_user,
                                        update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_instance_type, NEW.demand_instance_id, NEW.config_data,
               NEW.rel_template_Id, NEW.rel_scene_id, NEW.rel_group_id, NEW.create_time, NEW.create_user,
               NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# -------------------- create trigger demand_config_delete_snapshot --------------------
drop trigger if exists demand_config_delete_snapshot;
delimiter $$
create trigger demand_config_delete_snapshot
    after delete
    on demand_config
    for each row
begin
    update demand_config_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;

# ------------------------- create trigger demand_config_airconditioning_custom_update_snapshot -------------------------
drop trigger if exists demand_config_airconditioning_custom_update_snapshot;
delimiter $$
create trigger demand_config_airconditioning_custom_update_snapshot
    after update
    on demand_config_airconditioning_custom
    for each row
begin
    update demand_config_airconditioning_custom_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_config_airconditioning_custom_snapshot (id, project_id, demand_instance_type, demand_instance_id,
                                                               config_data, rel_template_id, rel_scene_id, create_time,
                                                               create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_instance_type, NEW.demand_instance_id, NEW.config_data,
               NEW.rel_template_id, NEW.rel_scene_id, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_config_airconditioning_custom_delete_snapshot -------------------------
drop trigger if exists demand_config_airconditioning_custom_delete_snapshot;
delimiter $$
create trigger demand_config_airconditioning_custom_delete_snapshot
    after delete
    on demand_config_airconditioning_custom
    for each row
begin
    update demand_config_airconditioning_custom_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;

# ------------------------- create trigger demand_config_group_update_snapshot -------------------------
drop trigger if exists demand_config_group_update_snapshot;
delimiter $$
create trigger demand_config_group_update_snapshot
    after update
    on demand_config_group
    for each row
begin
    update demand_config_group_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_config_group_snapshot (id, project_id, group_name, rel_template_Id, rel_scene_id, create_time,
                                              create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.group_name, NEW.rel_template_Id, NEW.rel_scene_id, NEW.create_time,
               NEW.create_user, NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# ------------------------- create trigger demand_config_group_delete_snapshot -------------------------
drop trigger if exists demand_config_group_delete_snapshot;
delimiter $$
create trigger demand_config_group_delete_snapshot
    after delete
    on demand_config_group
    for each row
begin
    update demand_config_group_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;

# -------------------- create trigger demand_instance_config_update_snapshot --------------------
drop trigger if exists demand_instance_config_update_snapshot;
delimiter $$
create trigger demand_instance_config_update_snapshot
    after update
    on demand_instance_config
    for each row
begin
    update demand_instance_config_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_instance_config_snapshot (id, project_id, demand_type, demand_template_type,
                                                 demand_instance_type, demand_instance_id, config_data, create_time,
                                                 create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_type, NEW.demand_template_type, NEW.demand_instance_type,
               NEW.demand_instance_id, NEW.config_data, NEW.create_time, NEW.create_user, NEW.update_time,
               NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# -------------------- create trigger demand_instance_config_delete_snapshot --------------------
drop trigger if exists demand_instance_config_delete_snapshot;
delimiter $$
create trigger demand_instance_config_delete_snapshot
    after delete
    on demand_instance_config
    for each row
begin
    update demand_instance_config_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
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
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
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
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;

# ------------------------- create trigger demand_template_update_snapshot -------------------------
drop trigger if exists demand_template_update_snapshot;
delimiter $$
create trigger demand_template_update_snapshot
    after update
    on demand_template
    for each row
begin
    update demand_template_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
    insert into demand_template_snapshot (id, project_id, demand_type, demand_template_type, demand_template_tag,
                                          demand_template_name, is_initialized, is_enabled, st, create_time,
                                          create_user, update_time, update_user, valid_end_time)
        value (NEW.id, NEW.project_id, NEW.demand_type, NEW.demand_template_type, NEW.demand_template_tag,
               NEW.demand_template_name, NEW.is_initialized, NEW.is_enabled, NEW.st, NEW.create_time, NEW.create_user,
               NEW.update_time, NEW.update_user, '9999-12-31 23:59:59');
end $$
delimiter ;
# -------------------- create trigger demand_template_delete_snapshot --------------------
drop trigger if exists demand_template_delete_snapshot;
delimiter $$
create trigger demand_template_delete_snapshot
    after delete
    on demand_template
    for each row
begin
    update demand_template_snapshot
    set valid_end_time = (select parse_project_timestamp(OLD.project_id))
    where id = OLD.id
      and valid_end_time = '9999-12-31 23:59:59';
end $$
delimiter ;