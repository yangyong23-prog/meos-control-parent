package com.persagy.apccontrol.basic.outside.feign.model.dto;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shaohongbo
 * @date 2023年03月29日 20:04
 */
@Data
public class DtpTwinIotData {

    private String objectId;

    private String classCode;

    private String infoCode;

    @JsonAlias({"dataTime", "twinTime"})
    private String dataTime;

    @JsonAlias({"dataValue", "twinValue"})
    private String dataValue;

    private String dataIndate;

    //数值标记1有效，2无效
    private Integer dataFlag;

    //1正常，2异常
    private Integer dataStatus;

    /*** 反馈值： 控制点强制状态  0-正常 1-强制(可为空)  控制值 0:未完成，1:成功,2:失败，3:超时，4:强制终止，5:控制异常 */
    private Integer forced;

    /***类型 0 反馈值 1 控制值*/
    private Integer type = 0;

    /**
     * 获取有效的IOT数据
     *
     * @param sourceList
     * @return
     */
    public static List<DtpTwinIotData> getVaildIotDatas(List<DtpTwinIotData> sourceList) {
        List<DtpTwinIotData> resultList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(sourceList)) {
            for (DtpTwinIotData iotData : sourceList) {
                if (iotData.getDataFlag() != null && iotData.getDataFlag() == 1 && iotData.getDataStatus() != null && iotData.getDataStatus() == 1) {
                    resultList.add(iotData);
                }
            }
        }
        return resultList;
    }

    /**
     * 获取有效的IOT数据
     *
     * @param groupCode
     * @param projectId
     * @param sourceList
     * @return
     */
    public static List<FeedbackInfoMsgMqttVO> getVaildIotDatas(String groupCode, String projectId, List<DtpTwinIotData> sourceList) {
        List<FeedbackInfoMsgMqttVO> resultList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(sourceList)) {
            for (DtpTwinIotData iotData : sourceList) {
                if (iotData.getDataFlag() != null && iotData.getDataFlag() == 1 && iotData.getDataStatus() != null && iotData.getDataStatus() == 1) {
                    FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = new FeedbackInfoMsgMqttVO(iotData.getObjectId(), iotData.getClassCode(), iotData.getInfoCode(), iotData.getDataTime(), iotData.getDataValue(), iotData.getDataStatus());
                    feedbackInfoMsgMqttVO.setDataIndate(Integer.valueOf(iotData.getDataIndate()));
                    feedbackInfoMsgMqttVO.setGroupCode(groupCode);
                    feedbackInfoMsgMqttVO.setProjectId(projectId);
                    resultList.add(feedbackInfoMsgMqttVO);
                }
            }
        }
        return resultList;
    }

}
