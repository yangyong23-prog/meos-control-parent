package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.service.param.equiprecord.EquipRecordQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author moshan
* @description 针对表【equip_record(设备冷热源切换记录表)】的数据库操作Mapper
* @createDate 2023-12-04 19:27:55
* @Entity com.persagy.apccontrol.basic.EquipRecord
*/
@Mapper
@Repository
public interface EquipRecordMapper extends BaseMapper<EquipRecord> {

    /**
     * 查询实体
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.8.0
     */
    List<EquipRecord> list(@NonNull @Param("param") EquipRecordQueryDaoParam param);

    /**
     * 查询最近一次运行状态
     * id 和 ids 必须传递一个
     *
     * @param param 查询参数
     * @return 是否运行
     * @since v5.8.0
     */
    String listRecentRunningStatus(@NonNull @Param("param") EquipRecordQueryDaoParam param);

    /**
     * @description: 更新执行时间
     * @author zyc
     * @param execTime 执行时间
     * @param updateTime 更新时间
     * @param updateIdList 记录ID
     * @param userId userID
     * @date 2023/12/6 10:40
     * @version 1.0
     */
    Boolean updateExecTimeByEqId(@Param("infoCode") String infoCode, @Param("execTime") String execTime,
                                 @Param("updateTime") String updateTime, @Param("userId") String userId,
                                 @Param("updateIdList") List<Integer> updateIdList, @Param("objIdList") List<String> objIdList,
                                 @Param("projectId") String projectId, @Param("groupCode") String groupCode);

    public List<EquipRecord> getEquipRecordByInfoCodeDescList(@Param("groupCode") String groupCode,
                                                              @Param("projectId") String projectId,
                                                              @Param("objId") List<String> objId,
                                                              @Param("infoCode") String infoCode,
                                                              @Param("execStatus") Integer execStatus
    );

    public List<EquipRecord> getEquipRecordByInfoCodeDesc(@Param("groupCode") String groupCode,
                                                          @Param("projectId") String projectId,
                                                          @Param("objId") List<String> objId,
                                                          @Param("infoCode") List<String> infoCode);


    /**
     * 查询清理指令表的数据
     * 1.清理执行失败的数据
     * 2.清理执行成功的数据 只保留最新一条
     */
    List<EquipRecord> queryDeleteEquipRecord();




}




