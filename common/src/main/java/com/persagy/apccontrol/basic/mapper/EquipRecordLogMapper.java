package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.EquipRecordLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author yuyao
 * @description 针对表【equip_record_log】的数据库操作Mapper
 * @createDate 2024-07-24 10:43:30
 * @Entity generator.domain.EquipRecordLog
 */
@Mapper
@Repository
public interface EquipRecordLogMapper extends BaseMapper<EquipRecordLog> {

}




