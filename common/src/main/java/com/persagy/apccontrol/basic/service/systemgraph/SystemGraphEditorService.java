package com.persagy.apccontrol.basic.service.systemgraph;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.*;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import org.springframework.lang.NonNull;

import java.util.LinkedList;
import java.util.List;

/**
 * 系统图编辑器服务
 *
 * @author MAF
 * @date 2024/06/26
 */
public interface SystemGraphEditorService extends LogComponent {

    //设备组内运行数量标签
    String EQUIP_GROUP_RUN_NUM_LABEL = "equipGroupRunNum";
    //设备组异常事件标签
    String EQUIP_GROUP_ABNORMAL_EVENT_NUM_LABEL = "equipGroupAbnormalEventNum";

    LinkedList<String> TAG_INFO_STRING_LIST = Lists.newLinkedList(
            Lists.newArrayList(
                    InfoCodeEnum.RUN_STATUS.getCode(),
                    InfoCodeEnum.ONLINE_STATUS.getCode(),
                    InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(),
                    InfoCodeEnum.MANUAL_AUTO_STATUS.getCode(),
                    InfoCodeEnum.FAULT_STATUS.getCode(),
                    InfoCodeEnum.CONTROL_FAILURE_ALARM.getCode(),
                    InfoCodeEnum.COOL_WATER_VLV_CONTROL_FAILURE_ALARM.getCode(),
                    InfoCodeEnum.CHILL_WATER_VLV_CONTROL_FAILURE_ALARM.getCode(),
                    InfoCodeEnum.SOURCE_SIDE_SWITCH_WATER_VALVE_CONTROL_FAILURE_ALARM.getCode(),
                    InfoCodeEnum.TERMINAL_SIDE_SWITCH_WATER_VALVE_CONTROL_FAILURE_ALARM.getCode(),
                    InfoCodeEnum.INDOOR_MEDIUM_VALVE_CONTROL_FAILURE_ALARM.getCode(),
                    InfoCodeEnum.GROUP_EQUIP_RUNING_NUM.getCode()
            )
    );

    /**
     * 标签高低液位报警信息点列表
     */
    List<String> TAG_HIGHT_LOW_LEVEL_ALARM_LIST = Lists.newArrayList(
            InfoCodeEnum.LOW_LEV_ALARM.getCode(),
            InfoCodeEnum.HIGH_LEV_ALARM.getCode()
    );

    /**
     * 高低液位报警显示classCode
     **/
    List<String> SHOW_HIGHT_LOW_LEVEL_ALARM_CLASS_CODE_LIST = Lists.newArrayList(
            DtpRwdClassCodeEnum.ACCCOT.getValue(),
            DtpRwdClassCodeEnum.ACCCWT.getValue(),
            DtpRwdClassCodeEnum.ACGEST.getValue(),
            DtpRwdClassCodeEnum.ACGEET.getValue(),
            DtpRwdClassCodeEnum.WSDWWT.getValue(),
            DtpRwdClassCodeEnum.WSDHWT.getValue(),
            DtpRwdClassCodeEnum.FFFSTP.getValue(),
            DtpRwdClassCodeEnum.FFFSWT.getValue()
    );

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

    /**
     * 列表系统模板
     *
     * @return {@link List }<{@link TemplateTreeVO }>
     */
    List<TemplateTreeVO> listSystemTemplate();

    /**
     * 初始化系统图
     *
     * @param dto   到
     * @param jobId
     * @return {@link List }<{@link SystemGraphDuplicateVO }>
     */
    List<SystemGraphDuplicateVO> initSystemGraph(RequestSystemGraphDTO dto, String jobId);

    boolean getIsNotEdit(DtpRwdObjectDTO dtpRwdObject, String graphTypeCode, String systemObjectId);

    /**
     * 获取副本列表
     *
     * @param dto 到
     * @return {@link List }<{@link SystemGraphDuplicateVO }>
     */
    List<SystemGraphDuplicateVO> listSystemGraphDuplicate(ListSystemGraphDuplicateDTO dto);

    /**
     * 创建系统图副本
     *
     * @param duplicateId 副本id
     * @return {@link SystemGraphDuplicateVO }
     */
    SystemGraphDuplicateVO createSystemGraphDuplicate(Long duplicateId);

    /**
     * 重新计算管道
     *
     * @param duplicateId 模板id
     * @param dto         到
     * @return {@link SystemGraphDuplicateVO }
     */
    SystemGraphDuplicateVO recalculatePipeline(Long duplicateId, RequestSystemGraphDTO dto);

    /**
     * 获取系统图数据
     *
     * @param duplicateId  到
     * @param isDetailData
     * @return {@link SystemGraphInstanceVo }
     */
    SystemGraphInstanceVo getSystemGraphData(Long duplicateId, boolean isDetailData);


    SystemGraphInstanceVo getSystemGraphDataNoDetailDataById(Long duplicateId);

    /**
     * 编辑系统图
     *
     * @param systemGraphDataId 系统图数据id
     * @param dto               到
     * @return {@link Boolean }
     */
    Boolean editSystemGraph(Long systemGraphDataId, List<UpdateGraphDTO> dto);

    /**
     * 发布系统图
     *
     * @param duplicateId 模板id
     * @return {@link Boolean }
     */
    Boolean publishSystemGraph(Long duplicateId);

    /**
     * 删除副本
     *
     * @param duplicateId 模板id
     * @return {@link Boolean }
     */
    Boolean deleteSystemGraphCopy(Long duplicateId);

    /**
     * 下架图纸
     *
     * @param duplicateId 模板id
     * @return {@link Boolean }
     */
    Boolean offlineSystemGraph(Long duplicateId);


    /**
     * @return java.lang.Boolean
     * @Author 薛涛
     * @Description 保存或者更新系统图细节调整
     * @Date 16:29 2024/6/27
     * @Param [systemGraphDetailVO]
     **/
    SystemGraphDetailVO saveAndUpdateSystemGraphDetail(SystemGraphDetailVO systemGraphDetailVO);

    /**
     * 图中设备已绑定的信息点全集
     *
     * @param listInfoCodeDTO 项目id，对象id、图名
     * @return 设备信息点全集
     */
    List<ListInfoCodeVo> listInfoCode(ListInfoCodeDTO listInfoCodeDTO);

    Boolean deleteSystemGraphDetail(SystemGraphDetailVO systemGraphDetailVO);

    List<SystemGraphDetailVO> getSystemGraphDetailById(SystemGraphDetailVO systemGraphDetailVO);

    /**
     * 根据副本id更新名称
     *
     * @param updateDuplicateNameVO 副本id 副本名称
     * @return 操作结果
     */
    Boolean editDuplicateName(UpdateDuplicateNameVO updateDuplicateNameVO);

    String updateTemplateName(UpdateTemplateNameDTO updateTemplateNameDTO);

    List<SystemGraphDetailVO> saveAndUpdateSystemGraphDetailBatch(List<SystemGraphDetailVO> systemGraphDetailVOs);

    Boolean copyDetailData(@NonNull CopyDetailDataParam param);
}
