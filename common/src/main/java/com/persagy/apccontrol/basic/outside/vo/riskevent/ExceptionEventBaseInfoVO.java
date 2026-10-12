package com.persagy.apccontrol.basic.outside.vo.riskevent;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.enums.ExceptionEventStatus;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.ImportanceDegreeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.ExceptionEventInstanceResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.ExceptionEventProblemResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 异常事件新的数据结构
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/30 16:07
 * @since 5.240402.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("异常事件基本信息")
public class ExceptionEventBaseInfoVO implements Serializable {
    private static final long serialVersionUID = -6202154465739454783L;

    @ApiModelProperty("对象ID")
    private String id;
    @ApiModelProperty("设备本地名称")
    private String localName;
    @ApiModelProperty("对象类型")
    private String objectType;
    @ApiModelProperty("所在空间ID")
    private Set<String> inSpaceIds;
    @ApiModelProperty("异常事件状态")
    private ExceptionEventStatus exceptionEventStatus;
    @ApiModelProperty("异常事件id")
    private String exceptionEventId;
    @ApiModelProperty("异常结束时间")
    private String exceptionEndTime;
    @ApiModelProperty("异常开始时间")
    private String exceptionStartTime;
    @ApiModelProperty("持续时长(分钟)")
    private Long duration;
    @ApiModelProperty("全部问题数量")
    private Integer allProblemCount;
    @ApiModelProperty("严重程度")
    private ImportanceDegreeEnum importanceDegree;
    @ApiModelProperty("已解决问题数量")
    private Integer resolvedProblemCount;
    @ApiModelProperty("实例信息")
    private List<ExceptionInstance> instances;
    @ApiModelProperty("问题信息")
    private List<ExceptionProblem> problems;
    @ApiModelProperty("一级管控目标集合")
    private LinkedHashSet<String> riskMeasureAimLevel1Collect;
    @ApiModelProperty("二级管控目标集合")
    private LinkedHashSet<String> riskMeasureAimLevel2Collect;
    //--------------------------250701新增字段start----------------------
    @ApiModelProperty("始发问题")
    private ExceptionEventProblemResponse originalProblem;

    @ApiModelProperty("最早要求解决时间")
    private LocalDateTime minRequireSolveTime;

    @ApiModelProperty("最晚要求解决时间")
    private LocalDateTime maxRequireSolveTime;
    @ApiModelProperty("异常事件是否已逾期")
    private Boolean overdue;

    @ApiModelProperty("关联风险事件")
    private List<SimpleRiskEventDTO> relatedRiskEvents;

    @ApiModelProperty("处理进程")
    private SolveProcessEnum solveProcess;

    /**
     * 事件编号
     */
    private String eventNumber;
    //--------------------------250701新增字段end----------------------


    // ------------------------- 自定义字段 -------------------------

    @ApiModelProperty("实例信息名称")
    private String instancesName;
    @ApiModelProperty("问题信息数量")
    private Integer problemsNumber;
    @ApiModelProperty("问题信息名称")
    private String problemsName;

    // ------------------------- 自定义字段 -------------------------


    public ExceptionEventBaseInfoVO(String id, String localName, ExceptionEventBaseInfoVO child) {
        BeanUtils.copyProperties(child, this);
        this.id = id;
        this.localName = localName;
    }

    /**
     * 填充实例信息
     *
     * @param exceptionEventObjMap 异常事件对象map
     *
     * @since 7.250701.0
     */
    public static void fillInstanceInfo(Map<String, List<ExceptionEventBaseInfoVO>> exceptionEventObjMap) {
        if (MapUtils.isEmpty(exceptionEventObjMap)) {
            return;
        }
        DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setId(exceptionEventObjMap.keySet());
        param.addWithColumnWithSimple(DtpRwdObjectQueryParam.Fields.id, DtpRwdObjectQueryParam.Fields.objType, DtpRwdObjectQueryParam.Fields.localName, DtpRwdObjectDTO.Fields.relObjIds);
        List<DtpRwdObjectDTO> objects = dtpRwdClientService.listObject(param);
        List<DtpRwdObjectDTO> equipments = objects.stream().filter(e -> ObjectTypeEnum.EQUIPMENT.value().equals(e.getObjType())).collect(Collectors.toList());
        Map<String, DtpRwdObjectDTO> objectDTOMap = objects.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity()));
        //如果是设备，查询所在空间
        Map<String, Set<String>> mapRelObjId = new HashMap<>();
        if (CollectionUtils.isNotEmpty(equipments)) {
            DtpRwdObjectQueryParam relSpaceParam = new DtpRwdObjectQueryParam();
            relSpaceParam.setClassCode(DtpRwdClassCodeEnum.SPACE);
            relSpaceParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.objType);
            relSpaceParam.setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjFrom(equipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet())));
            List<DtpRwdObjectDTO> spaces = dtpRwdClientService.listObject(relSpaceParam);
            if (CollectionUtils.isNotEmpty(spaces)) {
                Map<String, DtpRwdObjectDTO> spaceMap = spaces.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity()));
                objectDTOMap.putAll(spaceMap);
                mapRelObjId = DtpRwdObjectDTO.mapRelObjIdAndIds(spaces);
            }
        }
        if (MapUtils.isEmpty(objectDTOMap)) {
            return;
        }
        Map<String, Set<String>> finalMapRelObjId = mapRelObjId;
        for (Map.Entry<String, List<ExceptionEventBaseInfoVO>> entry : exceptionEventObjMap.entrySet()) {
            List<ExceptionEventBaseInfoVO> vos = entry.getValue();
            DtpRwdObjectDTO dtpRwdObjectDTO = objectDTOMap.get(entry.getKey());
            if (Objects.isNull(dtpRwdObjectDTO)) {
                continue;
            }
            vos.forEach(vo -> {
                ExceptionInstance instance = new ExceptionInstance();
                instance.setInstanceId(dtpRwdObjectDTO.getId());
                instance.setInstanceName(dtpRwdObjectDTO.getLocalName());
                instance.setClassCode(dtpRwdObjectDTO.getClassCode());
                instance.setObjectType(dtpRwdObjectDTO.getObjType());
                vo.setInstances(Lists.newArrayList(instance));
                vo.setInstancesName(dtpRwdObjectDTO.getLocalName());
                vo.setLocalName(dtpRwdObjectDTO.getLocalName());
                Set<String> relObjIds = finalMapRelObjId.get(dtpRwdObjectDTO.getId());
                vo.setInSpaceIds(relObjIds);
                vo.setObjectType(dtpRwdObjectDTO.getObjType());
                ExceptionEventProblemResponse originalProblem = new ExceptionEventProblemResponse();
                originalProblem.setInstanceName(dtpRwdObjectDTO.getLocalName());
                originalProblem.setInstanceId(dtpRwdObjectDTO.getId());
                originalProblem.setNodeName(vo.getProblemsName());
                //如果是空间，关联空间就是他本身
                List<ExceptionEventInstanceResponse> relatedSpaces = new ArrayList<>();
                if (ObjectTypeEnum.SPACE.value().equals(dtpRwdObjectDTO.getObjType())) {
                    ExceptionEventInstanceResponse response = new ExceptionEventInstanceResponse();
                    response.setInstanceName(dtpRwdObjectDTO.getLocalName());
                    response.setInstanceId(dtpRwdObjectDTO.getId());
                    response.setObjectType(dtpRwdObjectDTO.getObjType());
                    relatedSpaces.add(response);
                } else if (CollectionUtils.isNotEmpty(relObjIds)) {
                    for (String relObjId : relObjIds) {
                        DtpRwdObjectDTO relObj = objectDTOMap.get(relObjId);
                        if (Objects.isNull(relObj)) {
                            continue;
                        }
                        ExceptionEventInstanceResponse response = new ExceptionEventInstanceResponse();
                        response.setInstanceName(relObj.getLocalName());
                        response.setInstanceId(relObj.getId());
                        response.setObjectType(relObj.getObjType());
                        relatedSpaces.add(response);
                    }
                }
                originalProblem.setRelatedSpaces(relatedSpaces);
                vo.setOriginalProblem(originalProblem);
            });
            vos.sort(Comparator.comparing(ExceptionEventBaseInfoVO::getExceptionStartTime).reversed());
        }
    }

    /**
     * 针对异常事件id进行去重
     *
     * @param objectIdAbnormalEventMap 对象map集合
     * @return 去重后的map
     */
    public static Map<String, List<ExceptionEventBaseInfoVO>> duplicateRemoval(
            Map<String, List<ExceptionEventBaseInfoVO>> objectIdAbnormalEventMap) {
        Map<String, List<ExceptionEventBaseInfoVO>> result = new HashMap<>();
        Set<String> duplicateRemovalAbnormalEventIds = new HashSet<>();

        for (Map.Entry<String, List<ExceptionEventBaseInfoVO>> entry : objectIdAbnormalEventMap.entrySet()) {
            List<ExceptionEventBaseInfoVO> uniqueList = entry.getValue().stream()
                    .filter(obj -> duplicateRemovalAbnormalEventIds.add(obj.getExceptionEventId()))
                    .collect(Collectors.toList());
            result.put(entry.getKey(), uniqueList);
        }
        return result;
    }

    @ApiModel("异常事件详情异常问题信息")
    @Data
    public static class ExceptionProblem implements Serializable {
        private static final long serialVersionUID = 2714623145621633749L;
        @ApiModelProperty("异常问题id")
        private Long nodeId;
        @ApiModelProperty("异常问题名称")
        private String nodeName;

        public static ExceptionProblem of(@NonNull ExceptionEventProblemResponse response) {
            ExceptionProblem result = new ExceptionProblem();
            result.setNodeName(response.getNodeName());
            return result;
        }
    }

    @ApiModel("异常事件详情对象信息")
    @Data
    public static class ExceptionInstance implements Serializable {
        private static final long serialVersionUID = 6442128155638250102L;
        @ApiModelProperty("物理世界实例id")
        private String instanceId;
        @ApiModelProperty("物理世界实例名称")
        private String instanceName;
        @ApiModelProperty("对象类")
        private String classCode;
        @ApiModelProperty("对象类型")
        private String objectType;
        @ApiModelProperty("所在空间ID")
        @JsonAlias("spaceIds")
        @JSONField(alternateNames = "spaceIds")
        private Set<String> inSpaceIds;

        public static ExceptionInstance of(@NonNull ExceptionEventInstanceResponse response) {
            ExceptionInstance result = new ExceptionInstance();
            result.setInstanceId(response.getInstanceId());
            result.setInstanceName(response.getInstanceName());
            result.setInSpaceIds(response.getSpaceIds());
            result.setObjectType(response.getObjectType());
            return result;
        }
    }
}
