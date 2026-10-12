package com.persagy.apccontrol.common.service.command;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.event.lighting.InterventionLightingCurrentConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.common.service.command.batch.dto.CommandBatchExecuteDTO;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ManualAutoMaticSetService {

    /**
     * 获取自动状态
     *
     * @param objectId
     * @param infoCode
     * @return 返回true是自动 false 是手动
     */
    boolean getManualAutoMatic(String objectId, String infoCode);

    /**
     * 获取 需求 所以自动状态的对象信息点
     *
     * @return 对象id + 信息点数据
     */
    Set<String> getManualAutoMaticByList();

    /**
     * 获取 需求 所以自动状态的对象信息点
     *
     * @return 对象id + 信息点数据
     */
    Set<String> getManualAutoMaticByInfoCode(String infoCode);


    /**
     * 设置自动状态
     *
     * @param objectId
     * @param infoCode
     * @param ai       是否是自动 true是 false 为自动
     */
    void setManualAutoMatic(String objectId, String infoCode, boolean ai);

    /**
     * 设置为人工状态 添加干预标签
     *
     * @param dto
     */
    public void setManualAutoMatic(CommandBatchExecuteDTO dto);

    /**
     * 批量自动设置
     *
     * @param dto
     */
    public void setManualAutoMatic(List<CommandBatchExecuteDTO> dto);


    /**
     * 查询  人工干预的数据
     *
     * @param obj
     * @param infoCode
     * @return 干预截止时间
     */
    public LocalDateTime queryManualIntervention(String obj, String infoCode);

    /**
     * 查询所有人工干预的数据
     *
     * @return 干预截止时间
     */
    public Map<String, LocalDateTime> queryAllManualIntervention();


    /**
     * 设置人工干预
     *
     * @param dto
     */
    public void manualIntervention(CommandBatchExecuteDTO dto);


    /**
     * 解除对象的人工干预标签
     *
     * @param objectId
     */
    public void relieveManualIntervention(String objectId);

    /**
     * 解除干预
     *
     * @param objectId
     * @param infoCode
     */
    public void relieveManualIntervention(String objectId, String infoCode);


    /**
     * 获取所有人工干预标签
     */
    public Set<String> getAllManualInterventionKeyByInfoCode(String infoCode);


    /**
     * 重置需求为人工状态
     * 冷站/泵组
     */
    public void resetArtificialType(DemandTypeEnum type);

    @TransactionalEventListener(value = InterventionLightingCurrentConfigChangeEvent.class)
    default <T extends InterventionConfigData> void eventListener(InterventionLightingCurrentConfigChangeEvent<T> event) {
        event.getReleaseCommands().forEach(item -> relieveManualIntervention(item.getObjectId(), item.getInfoCode()));
    }
}
