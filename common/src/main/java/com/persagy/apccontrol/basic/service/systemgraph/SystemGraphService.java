package com.persagy.apccontrol.basic.service.systemgraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.opt.OptCallMemo;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.RequestSystemGraphDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphGenerateInstanceDto;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphInformationVo;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * @author persagy
 * @description 针对表【system_graph】的数据库操作Service
 * @createDate 2024-06-25 11:55:25
 */
public interface SystemGraphService extends IService<SystemGraph>, LogComponent {

    /**
     * 所有阀门的classCode
     */
    Set<String> ALL_VALVE_CLASS_CODES = Sets.newHashSet("FFVVGS", "FFVVWS", "FFVVSL", "FFVVEV", "FGFGSV", "TPVVOC", "TPVVEA",
            "FFVVGR", "FFVVBP", "FFVVSV", "FFVVGC", "FFVVDC", "FFVVWC", "FFVVWR", "ACVVNR", "ACVVNJ", "ACVVSV", "ACVVMV", "ACVVER",
            "ACVVNA", "ACVVEV", "ACVVTV", "ACVVEO", "ACVVDB", "ACVVTW", "ACVVEA", "ACGETH", "ACVVAV", "ACVVNO", "ACVVAC", "ACVVEJ",
            "ACVVRV", "ACVVSB", "ACVVFD", "ACVVWC", "FFVVTF", "FFVVER", "FFVVTS", "FFVVSF", "WSVVCV", "WSVVDV", "WSVVOV", "WSVVTV",
            "WSVVAV", "WSVVEV");

    /**
     * 需要显示设备组运行数量标签和设备组异常事件标签的classCode
     **/
    List<String> SHOW_EQUIP_GROUP_LABEL_CLASS_CODE_LIST = Lists.newArrayList(
            DtpRwdClassCodeEnum.ACCCC_GROUP.getValue(),
            DtpRwdClassCodeEnum.ACSCC_GROUP.getValue(),
            DtpRwdClassCodeEnum.ACOTC_GROUP.getValue(),
            DtpRwdClassCodeEnum.ACCPC_GROUP.getValue(),
            DtpRwdClassCodeEnum.ACOPC_GROUP.getValue(),
            DtpRwdClassCodeEnum.ACCGP_GROUP.getValue()
    );

    //固定标签
    List<String> fixedLabelInfoCodeList = Lists.newArrayList(InfoCodeEnum.RUN_STATUS.getCode(),
            InfoCodeEnum.ONLINE_STATUS.getCode(), InfoCodeEnum.ORDER_FAIL_ALARM.getCode(), InfoCodeEnum.POWER_STATUS.getCode());
    //小部件固定标签
    List<String> FIXED_PASSBY_INFOCODE_LIST = Lists.newArrayList(InfoCodeEnum.ONLINE_STATUS.getCode());
    //阀门开关状态
    List<String> FIXED_SWITCH_INFOCODE_LIST = Lists.newArrayList(InfoCodeEnum.SWITCH_STATUS.getCode());
    //集水坑 隔油池 餐饮除油成套设备classCode
    List<String> SPECIAL_CLASS_CODE_LIST = Arrays.asList(DtpRwdClassCodeEnum.WSSTCP.getValue(),
            DtpRwdClassCodeEnum.WSSTOS.getValue(), DtpRwdClassCodeEnum.WSSTOR.getValue());
    //需要添加run_status的classCode：空气源热泵控制组
    List<String> RUN_STATUS_CLASS_CODE_LIST = Lists.newArrayList(DtpRwdClassCodeEnum.ACAHC_GROUP.getValue());
    //集水坑 隔油池 餐饮除油成套设备infoCode
    List<String> SPECIAL_INFO_CODE_LIST = Arrays.asList(InfoCodeEnum.SUPER_HIGH_LEVEL_ALARM.getCode(),
            InfoCodeEnum.HIGH_LEVEL_ALARM.getCode());
    //需要显示柜号的设备类
    List<String> SHOW_PANEL_NO_CLASS_CODE_LIST = Arrays.asList("SETDHF", "SETDHI", "SETDHM", "SETDSA", "SETDSI", "SETDSM",
            "SETDSO", "SETDEA", "SETDEM", "SETDHC", "SETDHP", "SETDOF", "SETDPS", "SETDFV", "SETDLA", "SETDOS", "SETDRB",
            "SETDUP", "SETDTF", "SETDDS", "SETDLC", "SETDLI", "SETDDD", "SETDLB", "SETDLF", "SETDLM", "SETDLD", "SETDPF",
            "SETDIV"
    );

    SystemGraphInstanceVo initSystemGraphData(@NotNull RequestSystemGraphDTO graphGenerateLoadDto, Boolean isNotEdit, String jobId);

    void matchParallelLines(GraphGenerateInstanceDto graphGenerateInstanceDto);

    void getLineNode(SystemGraphInstanceVo systemGraphInstanceVo);

    void buildFixedLabelByPassBy(EquipmentNodeVo vo, Map<String, Set<String>> objectRuleMap, Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeInfoCodeMap);

    void buildFixedLabel(EquipmentNodeVo vo, Map<String, Set<String>> objectRuleMap, Map<String, List<GraphInformationVo>> graphInformationGroupByClassCode);

    void buildFixedLabelByGroup(GraphGroupVo vo, Map<String, Set<String>> objectRuleMap);

    void buildIconUrlVos(GraphLegendVo vo, Map<String, List<GraphIconVo>> iconMap);

    @Nullable
    String getGraphName(String classCode, String graphTypeCode, List<GraphConfigVo> graphConfig, String objectId);

    boolean buildPanelNodeInformationList(EquipmentNodeVo vo, String graphName, Map<String, Set<String>> objectRuleMap, Map<String, List<RcInfoCodeCfg>> mapListInfoCodeConfigs);

    void buildInteractiveConfig(EquipmentNodeVo vo, String graphName, List<InteractiveConfigVo> configVoList);

    /**
     * opt B10：同一张图内复用按 classCode 查询的对象列表；objectMemo 为 null 时与三参数版本一致
     */
    default void buildInteractiveConfig(EquipmentNodeVo vo, String graphName, List<InteractiveConfigVo> configVoList,
                                        @Nullable OptCallMemo<String, List<DtpRwdObjectDTO>> objectMemo) {
        buildInteractiveConfig(vo, graphName, configVoList);
    }

    void generateInteractiveConfig(EquipmentNodeVo vo, String dataObjectId, String classCode, InteractiveConfigVo interactiveConfigVo);

    void matchPipeLineStyle(SystemGraphInstanceVo result);

    SystemGraph saveSystemGraph(String graphName, String graphTypeCode, String classCode, String systemObjectId, Long systemGraphDuplicateId);

    @Nullable
    SystemGraph getSystemGraph(String systemObjectId, String classCode, String graphTypeCode);

    List<SystemGraph> listSystemGraph(String systemObjectId, String classCode);

    List<SystemGraph> listSystemGraph(String systemObjectId, Collection<String> classCodes, Collection<String> graphTypeCodes);

    boolean updateSystemGraphDuplicateIdById(Long id, Long systemGraphDuplicateId, String graphName);

    SystemGraph getSystemGraphByDuplicateId(Long id);

    boolean updateSystemGraphName(Long id, String graphName);

    void buildDrawerAllInfoCodes(EquipmentNodeVo vo, Map<String, Set<String>> objectRuleMap, Map<String, List<RcInfoCodeCfg>> mapListInfoCodeConfigs);

    Set<String> getAllEquipmentInSystemGraph(SystemGraphInstanceVo instanceVo);
}
