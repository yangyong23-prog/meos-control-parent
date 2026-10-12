package com.persagy.apccontrol.basic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.service.param.equiprecord.EquipRecordQueryDaoParam;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author moshan
 * @description 针对表【equip_record(设备冷热源切换记录表)】的数据库操作Service
 * @createDate 2023-12-04 19:27:55
 */
public interface EquipRecordService extends IService<EquipRecord> {

    /**
     * 查询实体
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.8.0
     */
    List<EquipRecord> list(@NonNull EquipRecordQueryDaoParam param);

    /**
     * 查询最近一次运行状态
     *
     * @param param 查询参数
     * @return 对象ID -> 是否运行
     * @since v5.8.0
     */
    Map<String, RunStatusEnum> mapRecentRunningStatus(@NonNull EquipRecordQueryDaoParam param);

    /**
     * @param addList    变更列表
     * @param systemCode 系统编码 ps:ACCI（冷热源）
     * @description: 新增或者修改设备记录, 执行规则：先根据设备ID删除没有执行的下发记录，让后再新增
     * @author zyc
     * @date 2023/12/6 20:10
     * @version 1.0
     */
    void saveOrUpdateRecord(List<EquipRecord> addList, String systemCode, String projectId);

    /**
     * 添加新的指令
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param classCode
     * @param infoCode
     * @param infoValue
     * @param expireTime
     * @param sf
     */
    void saveOrUpdateRecord(String groupCode, String projectId, String objectId, String classCode, String infoCode, String infoValue, String expireTime, SourceFromEnum sf);

    /**
     * 查询定时下发的列表
     *
     * @param groupCode
     * @param projectId
     * @param expireTime
     * @return
     */
    List<EquipRecord> queryBeExecutList(String groupCode, String projectId, String expireTime);

    /**
     * @description: 删除没有执行的记录
     * @author zyc
     * @date 2023/12/13 11:27
     * @version 1.0
     */
    Boolean deleteEquipRecord(List<EquipRecord> delList, String systemCode, String projectId, Collection<String> delInfoCode, SourceFromEnum sourceFromEnum, String startTime, String endTime);

    /**
     * @param addList     变更列表
     * @param systemCode  系统编码 ps:ACCI（冷热源）
     * @param delInfoCodes 需要删除指定信息点编码对应的数据
     * @description: 新增或者修改设备记录, 执行规则：先根据设备ID删除没有执行的下发记录，让后再新增
     * @author zyc
     * @date 2023/12/20 20:10
     * @version 1.0
     */
    void saveOrUpdateRecord(List<EquipRecord> addList, String systemCode, String projectId, Collection<String> delInfoCodes);

    /**
     * @description: 时间表变更下发记录
     * @param: sourceFromEnum 数据来源
     * @param: startTime 需要删除数据的开始时间
     * @param: endTime 需要删除数据的技术时间
     * @author zyc
     * @date: 2023/12/26 11:28
     */
    void saveOrUpdateRecordBySchedule(List<EquipRecord> addList, String systemCode,
                                      String projectId, SourceFromEnum sourceFromEnum,
                                      String startTime, String endTime);

    @Deprecated
    default EquipRecord getEquipRecordByInfoCodeDesc(String groupCode, String projectId, SourceFromEnum type, String objId, String infoCode, Integer execStatus) {
        return getEquipRecordByInfoCodeDesc(groupCode, projectId, type, objId, infoCode, null, execStatus);
    }

    /**
     * 查询之前最近一次的已执行指令的详情
     *
     * @param groupCode
     * @param projectId
     * @param type
     * @param objId
     * @param infoCode
     * @return
     */
    public EquipRecord getEquipRecordByInfoCodeDesc(String groupCode, String projectId, SourceFromEnum type, String objId, String infoCode, String execTime, Integer execStatus);

    /**
     * 批量查询最新的执行指令
     *
     * @param groupCode
     * @param projectId
     * @param objId
     * @param infoCode
     * @param execStatus
     * @return
     */
    public List<EquipRecord> getEquipRecordByInfoCodeDescList(String groupCode, String projectId, List<String> objId, String infoCode, Integer execStatus);

    /**
     * 查询已经执行的最新的最新记录
     *
     * @param groupCode 不可为空
     * @param projectId 不可为空
     * @param objId     不可为空
     * @param infoCode  可为空
     * @return
     */
    public List<EquipRecord> getEquipRecordByInfoCodeDesc(String groupCode, String projectId, List<String> objId, List<String> infoCode);

    /**
     * 根据来源，获取对应的指令下发数据
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @param infoCodes
     * @param sf
     */
    List<EquipRecord> queryEquipRecordBySourceFrom(String groupCode, String projectId, Set<String> objectIds, Set<String> infoCodes, SourceFromEnum sf);

    /**
     * 根据来源，删除对应的未执行指令信息
     *
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @param infoCodes
     * @param sf
     */
    Set<String> deleteEquipRecordBySourceFrom(String groupCode, String projectId, Set<String> objectIds, Set<String> infoCodes, SourceFromEnum sf);

    /**
     * 删除大于 执行时间未执行的 自动需求指令
     *
     * @param groupCode
     * @param projectId
     * @param objId
     * @param expireTime 执行时间
     */
    public void deleteEquipRecordByAiDemandExpireTime(String groupCode, String projectId, String objId, String expireTime, List<String> delInfos);


    /**
     * 删除未来未执行的指令
     *
     * @param groupCode
     * @param projectId
     * @param expireTime
     * @param systemCode
     */
    public void deleteLightDemandByExpireTime(String groupCode, String projectId,
                                              String expireTime, String systemCode, Boolean ai);

    /**
     * 删除时间表未来指令
     *
     * @param groupCode
     * @param projectId
     * @param objId
     * @param expireTime
     */
    void deleteEquipRecordByScheduleExpireTime(String groupCode, String projectId, String objId, String expireTime);


    EquipRecord queryByExpireTime(String groupCode, String projectId, String objId, SourceFromEnum type, String expireTime, String infoCode);

    /**
     * 通过批次号和对象 ID 获取数据
     *
     * @param objIds
     * @param batchNumber
     * @return
     */
    List<EquipRecord> queryByBatchNumber(List<String> objIds, String batchNumber);

}
