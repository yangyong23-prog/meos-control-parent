alter table strategy_cfg add sync_time datetime null comment '同步时间';
alter table strategy_cfg add sync_message text null comment '同步信息';

alter table strategy_date_cfg add sync_time datetime null comment '同步时间';
alter table strategy_date_cfg add sync_message text null comment '同步信息';

alter table strategy_plan_cfg add sync_time datetime null comment '同步时间';
alter table strategy_plan_cfg add sync_message text null comment '同步信息'