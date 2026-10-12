package com.persagy.apccontrol.common.service;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.EnumIotDataType;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;

import java.util.Collection;
import java.util.List;

/**
 * @author shaohongbo
 * @date 2023/4/4 19:11
 */
public interface IotDataCacheService {

    /**
     * 所有用这个方法的主要了,好像都是有问题的.萌萌说了 动态数据都判断5min内 凡是用到的建议都改成 queryIotRealDataForFiveMin
     *
     * @param objectId
     * @param infoCode
     * @return
     */
    String queryIotRealData(String objectId, String infoCode, RcInfoCodeCfg infoCodeCfg);

    /**
     * 批量查询信息点数据
     *
     * @param objectId
     * @param infoCode
     * @return
     */
    List<FeedbackInfoMsgMqttVO> queryIotRealDataList(Collection<String> objectId, List<String> infoCode);

    /**
     * 数据进行二次处理
     *
     * @param objectIdList
     * @param infoCodeList
     * @return
     */
    List<FeedbackInfoMsgMqttVO> queryExtendIotRealDataList(Collection<String> objectIdList, List<String> infoCodeList);

    /**
     * 机器人模块专用（临时方法）
     *
     * @param objectIdList
     * @param infoCodeList
     * @return {@link List<FeedbackInfoMsgMqttVO>}
     * @author yuecaipu
     * @date 2024/7/25 18:44
     * @since
     */
    List<FeedbackInfoMsgMqttVO> queryIotRealDataListFromCache(Collection<String> objectIdList, Collection<String> infoCodeList);

    /**
     * @description: 查询5min之内的数据
     * @author zyc
     * @date 2023/12/22 19:11
     * @version 1.0
     */
    String queryIotRealDataForFiveMin(String objectId, String infoCode, RcInfoCodeCfg infoCodeCfg);

    String queryIotCtrlData(String objectId, String infoCode);

    /**
     * 字符串 类型的缓存数据
     */
    void setStringCacheData(String redisKey, String value);

    String getStringCacheData(String redisKey);

    void insert(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeCfg);

    /**
     * 查询实时反馈值
     *
     * @param objectId
     * @param infoCode
     * @return com.persagy.apccontrol.model.vo.FeedbackInfoMsgMqttVO
     * @author shaohongbo
     * @date 2023/4/14 16:25
     */
    FeedbackInfoMsgMqttVO queryIotRealDataVO(String objectId, String infoCode, RcInfoCodeCfg infoCodeCfg);

    /**
     * 查询实时反馈值
     *
     * @param objectId
     * @param infoCode
     * @return com.persagy.apccontrol.model.vo.FeedbackInfoMsgMqttVO
     * @author shaohongbo
     * @date 2023/4/14 16:25
     */
    FeedbackInfoMsgMqttVO queryIotRealDataRedisVO(String objectId, String infoCode);

    /**
     * 查询对象下的 反馈信息集合
     *
     * @param objectId
     * @return
     */
    List<FeedbackInfoMsgMqttVO> queryIotRealDataVO(String objectId, List<String> infoCodes);

    /**
     * 查询控制值
     *
     * @param objectId
     * @param infoCode
     * @return com.persagy.apccontrol.model.vo.FeedbackInfoMsgMqttVO
     * @author shaohongbo
     * @date 2023/4/14 16:25
     */
    FeedbackInfoMsgMqttVO queryIotCtrlDataVO(String objectId, String infoCode, boolean success);

    /**
     * 根据数据类型查询缓存中对应值
     *
     * @param objectId
     * @param infoCode
     * @param iotDataType
     * @return com.persagy.apccontrol.model.vo.FeedbackInfoMsgMqttVO
     * @author shaohongbo
     * @date 2023/4/14 16:18
     */
    FeedbackInfoMsgMqttVO queryIotDataVOByDataType(String objectId, String infoCode, RcInfoCodeCfg infoCodeCfg, EnumIotDataType iotDataType);


    void setDataValueAndTime(FeedbackInfoMsgMqttVO infoMsgMqttVO, RcInfoCodeCfg rcInfoCodeCfg, JSONObject objectInfo);

}
