package com.persagy.apccontrol.basic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.EquipRecordLogMapper;
import com.persagy.apccontrol.basic.model.entity.EquipRecordLog;
import com.persagy.apccontrol.basic.service.EquipRecordLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author yuyao
 * @description 针对表【equip_record_log】的数据库操作Service
 * @createDate 2024-07-24 10:43:30
 */
@Service
@Slf4j
public class EquipRecordLogServiceImpl extends ServiceImpl<EquipRecordLogMapper, EquipRecordLog> implements EquipRecordLogService {

}
