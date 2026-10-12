package com.persagy.apccontrol.common.service.command;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.RedisCacheExpUtil;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyInfoCodeEnum;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.common.model.dto.CommandExecuteDTO;
import com.persagy.apccontrol.common.service.command.batch.dto.CommandBatchExecuteDTO;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description: 获取手自动查询设置
 * @Author: laixingqi
 * @Date: 2023/12/22 15:18
 * @param:
 * @return:
 **/
@Service
@Slf4j
public class ManualAutoMaticSetServiceImpl implements ManualAutoMaticSetService {

    @Resource
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private RedisCacheExpUtil redisCacheExpUtil;
    @Resource
    private EquipRecordService equipRecordService;

    @Resource
    private ProjectCfg projectCfg;

    private static final String LOG_TAG = "手自动设置";

    /**
     * @Description: 获取手自动
     * @Author: laixingqi
     * @Date: 2023/12/22 15:18
     * @param: [objectId, infoCode]
     * @return: int
     **/
    @Override
    public boolean getManualAutoMatic(String objectId, String infoCode) {
        String key = RedisKey.getAutomatic(objectId, infoCode);
        Object value = redisCacheUtil.get(key);

        boolean auto = value != null;
        //如果不是开关机信息点 判断是否存在人工干预，没有人工干预 补偿为自动模式
        if (!AutoMaticInfoCodeEnum.switchState(infoCode)) {
            LocalDateTime e = queryManualIntervention(objectId, infoCode);

            //不存在人工干预数据 并且不是自动 返回自动
            if (e == null && !auto) {
                //状态恢复为自动
                setManualAutoMatic(objectId, infoCode, true);
                return true;
            }
        }
        return auto;
    }

    @Override
    public Set<String> getManualAutoMaticByList() {
        return redisCacheExpUtil.scanKeys(RedisKey.APC_AUTOMATIC_KEY + "*");
    }


    @Override
    public Set<String> getManualAutoMaticByInfoCode(String infoCode) {
        return redisCacheExpUtil.scanKeys(RedisKey.APC_AUTOMATIC_KEY + "*" + infoCode + "*");
    }

    /**
     * @Description: 设置手自动
     * @Author: laixingqi
     * @Date: 2023/12/22 15:18
     * @param: [objectId, infoCode]
     * @return: int
     **/
    @Override
    public void setManualAutoMatic(String objectId, String infoCode, boolean ai) {
        //大小机信息点特殊处理
        infoCode = StrategyInfoCodeEnum.getChillerNumByInfoCode(infoCode);

        String key = RedisKey.getAutomatic(objectId, infoCode);
        if (ai) {
            redisCacheUtil.set(key, 1);
        } else {
            //切换为手动的时候需要把未来为执行的自动指令删除
            String expireTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
            equipRecordService.deleteEquipRecordByAiDemandExpireTime(projectCfg.getGroupCode(), projectCfg.getProjectId(), objectId, expireTime, Collections.singletonList(infoCode));
            //删除自动标识
            redisCacheUtil.del(key);
        }

        LogExecutor.instance().info(log, LOG_TAG, "设置手自动状态", Arrays.asList(
                Pair.of("objectId", objectId),
                Pair.of("infoCode", infoCode),
                Pair.of("ai", ai)
        ));
    }

    /**
     * 设置为人工状态 添加干预标签
     *
     * @param dto
     */
    @Override
    public void setManualAutoMatic(CommandBatchExecuteDTO dto) {
        //设置对象为人工标识
        this.setManualAutoMatic(dto.getObjectId(), dto.getInfoCode(), false);
        //开关机状态不设置人工干预标签
        if (DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode().equals(dto.getInfoCode()) ||
                DashBoardInfoCodeEnum.ENABLE_CTRL.getCode().equals(dto.getInfoCode())
        ) {
            return;
        }
        this.manualIntervention(dto);
    }

    @Override
    public void setManualAutoMatic(List<CommandBatchExecuteDTO> dto) {
        dto.forEach(this::setManualAutoMatic);
    }

    @Override
    public LocalDateTime queryManualIntervention(String obj, String infoCode) {

        //大小机信息点特殊处理
        infoCode = StrategyInfoCodeEnum.getChillerNumByInfoCode(infoCode);

        String key = RedisKey.systemIntervene(obj, infoCode);
        Object o = redisCacheUtil.get(key);

        return getManualInterventionLocalDateTime(o);
    }

    /**
     * @param o redis 缓存对象
     * @return
     * @Description: 获取人工干预时间
     */
    private LocalDateTime getManualInterventionLocalDateTime(Object o) {

        if (o == null) {
            return null;
        }

        //对历史数据进行是配置
        if (o instanceof String) {
            return LocalDateTime.parse(o.toString(), DatePattern.NORM_DATETIME_FORMATTER);
        }
        CommandExecuteDTO d = BeanUtil.toBean(o, CommandExecuteDTO.class);
        return d.getTimeDuration();
    }

    @Override
    public Map<String, LocalDateTime> queryAllManualIntervention() {
        //获取所有key
        Set<String> keyList = redisCacheExpUtil.scanKeys(RedisKey.APC_SYSTEM_INTERVENE_KEY + CommonConst.COLON + "*");
        List<String> keys = new ArrayList<>(keyList);
        //获取key的值
        List<Object> vals = redisCacheUtil.mGet(keys);
        Map<String, LocalDateTime> map = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            map.put(keys.get(i), getManualInterventionLocalDateTime(vals.get(i)));
        }

        return map;
    }

    @Override
    public void manualIntervention(CommandBatchExecuteDTO dto) {
        //大小机信息点特殊处理
        String infoCode = StrategyInfoCodeEnum.getChillerNumByInfoCode(dto.getInfoCode());

        String key = RedisKey.systemIntervene(dto.getObjectId(), infoCode);

        if (!dto.getAi()) {
            //先删除 redis 的数据
            redisCacheUtil.del(key);
            //解除干预
            if (dto.getReleaseIntervention()) {
                return;
            }
            redisCacheUtil.set(key, dto.getTimeDuration());
        }

        LogExecutor.instance().info(log, LOG_TAG, "设置人工干预", Arrays.asList(
                Pair.of("key", key),
                Pair.of("dto", dto)
        ));
    }

    @Override
    public void relieveManualIntervention(String objectId) {
        //获取该对象所有人工干预数据
        Set<String> keyList = redisCacheExpUtil.scanKeys(RedisKey.APC_SYSTEM_INTERVENE_KEY + CommonConst.COLON + objectId + "*");
        if (CollectionUtil.isNotEmpty(keyList)) {
            //进行删除
            redisCacheUtil.del(keyList);
        }

        LogExecutor.instance().info(log, LOG_TAG, "解除对象的人干预", Collections.singletonList(
                Pair.of("objectId", objectId)
        ));
    }

    @Override
    public void relieveManualIntervention(String objectId, String infoCode) {
        //大小机信息点特殊处理
        infoCode = StrategyInfoCodeEnum.getChillerNumByInfoCode(infoCode);
        String key = RedisKey.systemIntervene(objectId, infoCode);
        //进行删除
        redisCacheUtil.del(key);

        LogExecutor.instance().info(log, LOG_TAG, "解除干预", Arrays.asList(
                Pair.of("objectId", objectId),
                Pair.of("infoCode", infoCode)
        ));
    }

    @Override
    public Set<String> getAllManualInterventionKeyByInfoCode(String infoCode) {
        return redisCacheExpUtil.scanKeys(RedisKey.APC_SYSTEM_INTERVENE_KEY + "*" + infoCode + "*");
    }

    @Override
    public void resetArtificialType(DemandTypeEnum type) {

        switch (type) {
            case AIRCONDITIONING:

                //删除redis手自动 只删除开关机状态
                Set<String> rs = redisCacheExpUtil.scanKeys(RedisKey.APC_AUTOMATIC_KEY + "*" + AutoMaticInfoCodeEnum.RUN_STATUS.getPracticalCode());
                Set<String> ec = redisCacheExpUtil.scanKeys(RedisKey.APC_AUTOMATIC_KEY + "*" + AutoMaticInfoCodeEnum.ENABLE_CTRL.getPracticalCode());
                Set<String> ch = redisCacheExpUtil.scanKeys(RedisKey.APC_AUTOMATIC_KEY + "*" + AutoMaticInfoCodeEnum.COLD_HOT_SYS_SWITCH_SET.getPracticalCode());
                if (CollectionUtil.isNotEmpty(rs)) {
                    redisCacheUtil.del(rs);
                }
                if (CollectionUtil.isNotEmpty(ec)) {
                    redisCacheUtil.del(ec);
                }
                if (CollectionUtil.isNotEmpty(ch)) {
                    redisCacheUtil.del(ch);
                }
                break;
            case LIGHTING:
                Set<String> cd = redisCacheExpUtil.scanKeys(RedisKey.APC_AUTOMATIC_KEY + "*" + AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode());
                if (CollectionUtil.isNotEmpty(cd)) {
                    redisCacheUtil.del(cd);
                }
                break;
            default:
        }

        LogExecutor.instance().info(log, LOG_TAG, "重置需求为人工状态", Arrays.asList(
                Pair.of("type", type)));

    }

}
