package com.persagy.apccontrol.basic.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.convert.EquipConvert;
import com.persagy.apccontrol.basic.mapper.EquipRecordMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.service.EquipRecordLogService;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.basic.service.param.equiprecord.EquipRecordQueryDaoParam;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author moshan
 * @description 针对表【equip_record(设备冷热源切换记录表)】的数据库操作Service实现
 * @createDate 2023-12-04 19:27:55
 */
@Service
@Slf4j
public class EquipRecordServiceImpl extends ServiceImpl<EquipRecordMapper, EquipRecord> implements EquipRecordService {


    @Autowired
    private EquipRecordLogService equipRecordLogService;
    @Autowired
    private EquipConvert equipConvert;

    @Override
    public Map<String, RunStatusEnum> mapRecentRunningStatus(@NonNull EquipRecordQueryDaoParam param) {
        param.setEndExecuteTime(LocalDateTime.now());
        Map<String, RunStatusEnum> result = new HashMap<>();

        if (CollectionUtils.isNotEmpty(param.getObjIds())) {

            List<EquipRecord> equipRecordList = getEquipRecordByInfoCodeDesc(param.getGroupCode(), param.getProjectId(),
                    new ArrayList<>(param.getObjIds()), Collections.singletonList(param.getChangeContent().getInfoCode()));

            param.getObjIds().forEach(i -> equipRecordList.forEach(b -> {
                if (StringUtils.equals(b.getObjId(), i)) {
                    result.put(i, StringUtils.equals(b.getChangeContent().getInfoValue(), "1") ? RunStatusEnum.OPEN : RunStatusEnum.CLOSE);
                }
            }));
        } else {
            String objectIdAndRecentRunningStatus = getBaseMapper().listRecentRunningStatus(param);
            result.put(param.getObjId(), StringUtils.equals(objectIdAndRecentRunningStatus, "1") ? RunStatusEnum.OPEN : RunStatusEnum.CLOSE);
        }
        return result;
    }

    @Override
    public List<EquipRecord> list(@NonNull EquipRecordQueryDaoParam param) {
        param.setGroupCode(StringUtils.defaultString(param.getGroupCode(), ContextUtil.getGroupCode()));
        return getBaseMapper().list(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateRecord(List<EquipRecord> addList, String systemCode, String projectId) {
        // 先删除之前的，让后再新增
        saveOrUpdate(addList, systemCode, projectId, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateRecord(String groupCode, String projectId, String objectId, String classCode, String infoCode, String infoValue, String expireTime, SourceFromEnum sf) {
        // 1.先删除历史的
        this.deleteEquipRecordBySourceFrom(groupCode, projectId, Sets.newHashSet(objectId), Sets.newHashSet(infoCode), sf);

        // 2.添加新的
        EquipRecord equipRecord = new EquipRecord(groupCode, projectId, null, null, objectId, classCode, new ChangeContent(expireTime, infoCode, infoValue),
                ContextUtil.getUserId(), DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN));
        equipRecord.getChangeContent().setSourceFrom(sf);
        this.save(equipRecord);
    }

    @Override
    public List<EquipRecord> queryBeExecutList(String groupCode, String projectId, String expireTime) {
        //排除 1 分钟纬度的数据
        return this.lambdaQuery()
                .eq(EquipRecord::getGroupCode, groupCode).eq(EquipRecord::getProjectId, projectId).eq(EquipRecord::getExecStatus, -1)
                .apply(StringUtils.isNotBlank(expireTime), "change_content ->> '$.execTime' <= '" + expireTime + "'")
                //.ne(EquipRecord::getInfoCode, InfoCodeEnum.RUN_MODEL_SET.getCode())
                .apply("change_content ->> '$.sourceFrom' != '" + SourceFromEnum.RELEASE_INTERVENTION.value() + "'")
                .apply("change_content ->> '$.sourceFrom' != '" + SourceFromEnum.OTHER.value() + "'")
                .list();
    }

    @Override
    public void saveOrUpdateRecord(List<EquipRecord> addList, String systemCode, String projectId, Collection<String> delInfoCodes) {
        saveOrUpdate(addList, systemCode, projectId, delInfoCodes);
    }

    @Override
    public void saveOrUpdateRecordBySchedule(List<EquipRecord> addList, String systemCode,
                                             String projectId, SourceFromEnum sourceFromEnum,
                                             String startTime, String endTime) {

        if (CollectionUtils.isEmpty(addList)) {
            return;
        }

        // 先删除之前的，让后再新增
        this.deleteEquipRecord(addList, systemCode, projectId, null, sourceFromEnum, startTime, endTime);

        boolean saveFlag = this.saveBatch(addList);
        if (saveFlag) {
            log.info("【运行模式切换】记录手自动切换记录成功!");
        }
    }

    private void saveOrUpdate(List<EquipRecord> addList, String systemCode, String projectId, Collection<String> delInfoCodes) {

        if (CollectionUtils.isEmpty(addList)) {
            return;
        }

        // 先删除之前的，让后再新增
        this.deleteEquipRecord(addList, systemCode, projectId, delInfoCodes, null, null, null);

        boolean saveFlag = this.saveBatch(addList);
        if (saveFlag) {
            log.info("【异步指令下发】保存成功!");
        }
    }

    @Override
    public Boolean deleteEquipRecord(List<EquipRecord> delList, String systemCode, String projectId, Collection<String> infoCodes,
                                     SourceFromEnum sourceFromEnum, String startTime, String endTime) {

        if (CollectionUtils.isEmpty(delList)) {
            return true;
        }
        //取出指令类型模版
        if (sourceFromEnum == null && delList.get(0).getChangeContent() != null) {
            sourceFromEnum = delList.get(0).getChangeContent().getSourceFrom();
        }

        // 先删除之前的，让后再新增
        Set<String> eqIdSet = delList.stream().map(EquipRecord::getObjId).collect(Collectors.toSet());
        LambdaQueryWrapper<EquipRecord> lambdaQueryWrapper = Wrappers.<EquipRecord>lambdaQuery()
                .eq(StrUtil.isNotBlank(systemCode), EquipRecord::getSystemCode, systemCode)
                .eq(EquipRecord::getGroupCode, ContextUtil.getGroupCode())
                .eq(EquipRecord::getProjectId, projectId)
                .eq(EquipRecord::getExecStatus, "-1")
                .in(EquipRecord::getObjId, eqIdSet)
                .in(CollectionUtils.isNotEmpty(infoCodes), EquipRecord::getInfoCode, infoCodes)
                .apply(StringUtils.isNotBlank(startTime), "change_content->>'$.execTime' >=" + startTime)
                .apply(StringUtils.isNotBlank(endTime), "change_content->>'$.execTime' <" + endTime);
        if (sourceFromEnum != null) {
            lambdaQueryWrapper.apply("JSON_CONTAINS(change_content, JSON_OBJECT('sourceFrom', '" + sourceFromEnum.getCode() + "'))");
        }
        return this.remove(lambdaQueryWrapper);

    }


    @Override
    public EquipRecord getEquipRecordByInfoCodeDesc(String groupCode, String projectId, SourceFromEnum type, String objId, String infoCode, String execTime, Integer execStatus) {

        LambdaQueryChainWrapper<EquipRecord> qw = this.lambdaQuery()
                .eq(EquipRecord::getGroupCode, groupCode)
                .eq(EquipRecord::getProjectId, projectId)
                .eq(EquipRecord::getObjId, objId)
                .eq(execStatus != null, EquipRecord::getExecStatus, execStatus)
                .apply(StringUtils.isNotBlank(execTime), "change_content->>'$.execTime' =" + execTime)
                .apply("change_content->>'$.infoCode' = '" + infoCode + "'");

        if (type != null && !InfoCodeEnum.MANUAL_AUTO_SET.getCode().equals(infoCode)) {
            qw.apply("change_content->>'$.sourceFrom' = '" + type.getCode() + "'");
        }
        return qw.last(" order by change_content ->> '$.execTime' desc " + CommonConst.LIMIT_ONE).one();
    }

    @Override
    public List<EquipRecord> getEquipRecordByInfoCodeDescList(String groupCode, String projectId, List<String> objId, String infoCode, Integer execStatus) {

        if (CollectionUtils.isEmpty(objId)) {
            return new ArrayList<>();
        }
        return this.getBaseMapper().getEquipRecordByInfoCodeDescList(groupCode, projectId, objId, infoCode, execStatus);
    }

    @Override
    public List<EquipRecord> getEquipRecordByInfoCodeDesc(String groupCode, String projectId, List<String> objId, List<String> infoCode) {

        if (CollectionUtils.isEmpty(objId)) {
            return new ArrayList<>();
        }
        return this.getBaseMapper().getEquipRecordByInfoCodeDesc(groupCode, projectId, objId, infoCode);
    }

    @Override
    public List<EquipRecord> queryEquipRecordBySourceFrom(String groupCode, String projectId, Set<String> objectIds, Set<String> infoCodes, SourceFromEnum sf) {
        LambdaQueryWrapper<EquipRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(EquipRecord::getGroupCode, groupCode).eq(EquipRecord::getProjectId, projectId)
                .in(CollectionUtil.isNotEmpty(objectIds), EquipRecord::getObjId, objectIds)
                .in(CollectionUtil.isNotEmpty(infoCodes), EquipRecord::getInfoCode, infoCodes)
                .eq(EquipRecord::getExecStatus, "-1")
                .apply("change_content->>'$.sourceFrom' = '" + sf.getCode() + "'");
        return this.list(qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Set<String> deleteEquipRecordBySourceFrom(String groupCode, String projectId, Set<String> objectIds, Set<String> infoCodes, SourceFromEnum sf) {
        LambdaQueryWrapper<EquipRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(EquipRecord::getGroupCode, groupCode).eq(EquipRecord::getProjectId, projectId)
                .in(CollectionUtil.isNotEmpty(objectIds), EquipRecord::getObjId, objectIds)
                .in(CollectionUtil.isNotEmpty(infoCodes), EquipRecord::getInfoCode, infoCodes)
                .eq(EquipRecord::getExecStatus, "-1")
                .apply("change_content->>'$.sourceFrom' = '" + sf.getCode() + "'");
        this.remove(qw);
        return null;
    }

    @Override
    public void deleteEquipRecordByAiDemandExpireTime(String groupCode, String projectId, String objId, String expireTime, List<String> delInfos) {
        removeByTime(groupCode, projectId, objId, expireTime, delInfos, SourceFromEnum.AI_DEMAND);
    }

    @Override
    public void deleteLightDemandByExpireTime(String groupCode, String projectId, String expireTime, String systemCode, Boolean ai) {
        LambdaQueryWrapper<EquipRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(EquipRecord::getGroupCode, groupCode)
                .eq(EquipRecord::getProjectId, projectId)
                .eq(EquipRecord::getExecStatus, "-1")
                .like(EquipRecord::getSystemCode, systemCode)
                .apply("change_content->>'$.execTime' >= '" + expireTime + "'")
                .apply("change_content->>'$.ai' >= '" + ai + "'");
        this.remove(qw);
    }

    private void removeByTime(String groupCode, String projectId, String objId, String expireTime, List<String> delInfos, SourceFromEnum sf) {
        LambdaQueryWrapper<EquipRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(EquipRecord::getGroupCode, groupCode)
                .eq(EquipRecord::getProjectId, projectId)
                .eq(EquipRecord::getObjId, objId)
                .in(CollectionUtils.isNotEmpty(delInfos), EquipRecord::getInfoCode, delInfos)
                .eq(EquipRecord::getExecStatus, "-1")
                .apply("change_content->>'$.execTime' >= '" + expireTime + "'")
                .apply("change_content->>'$.sourceFrom' = '" + sf.getCode() + "'");
        //删除当前时间后未来24小时的执行指令
        this.remove(qw);
    }

    @Override
    public void deleteEquipRecordByScheduleExpireTime(String groupCode, String projectId, String objId, String expireTime) {
        removeByTime(groupCode, projectId, objId, expireTime, null, SourceFromEnum.SCHEDULE);
    }

    @Override
    public EquipRecord queryByExpireTime(String groupCode, String projectId, String objId, SourceFromEnum type, String expireTime, String infoCode) {
        LambdaQueryWrapper<EquipRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(EquipRecord::getGroupCode, groupCode)
                .eq(EquipRecord::getProjectId, projectId)
                .eq(EquipRecord::getObjId, objId)
                .eq(EquipRecord::getInfoCode, infoCode)
                .apply("change_content->>'$.execTime' >= '" + expireTime + "'")
                .apply("change_content->>'$.sourceFrom' = '" + type.getCode() + "'")
                .last(CommonConst.LIMIT_ONE);
        //删除当前时间后未来24小时的执行指令
        return this.getOne(qw);
    }

    @Override
    public List<EquipRecord> queryByBatchNumber(List<String> objIds, String batchNumber) {
        return this.lambdaQuery().in(EquipRecord::getObjId, objIds)
                .like(EquipRecord::getChangeContent, batchNumber)
                .list();
    }


}




