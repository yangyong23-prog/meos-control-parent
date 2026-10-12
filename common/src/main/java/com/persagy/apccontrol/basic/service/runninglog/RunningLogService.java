package com.persagy.apccontrol.basic.service.runninglog;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.runnningLog.*;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.model.vo.log.RunningLogHomePageVO;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogEquipmentListQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogPageQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogQueryParam;
import com.persagy.framework.common.entity.R;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RunningLogService {

    /**
     * 日志分类查询接口
     * @return
     */
    List<RunningLogCategoryDto> getLogCategoryList();

    /**
     * 获取一级日志分类下的所有二级菜单
     *
     * @param firstLevelCategoryCodes
     * @return
     */
    List<JSONObject> listSecondLevelCategory(Set<String> firstLevelCategoryCodes);

    /**
     * 查询日志列表
     * @param param
     * @return
     */
    List<RunningLogDto> listRunningLogByDate(@NonNull RunningLogQueryParam param);

    /**
     * 查询日志列表
     * @param param
     * @return
     */
    R<Collection<RunningLogDto>> queryLogList(@NonNull RunningLogPageQueryParam param);

    /**
     * 对外接口--查询日志列表
     * @param param
     * @return
     */
    List<RunningLogResDto> queryLogListByParam(@NonNull RunningLogPageQueryParam param);
    /**
     * 设备列表
     * @param param
     * @return
     */
    List<RunningLogEquipmentDto> equipmentList(@NonNull RunningLogEquipmentListQueryParam param);

    /**
     * 日志下载
     * @param param
     * @return
     */
    String export(@NonNull RunningLogPageQueryParam param);


    public static List<RunningLogDto> getRunningLogDtos(List<RcRunningLog> rcRunningLogs) {
        List<RunningLogDto> logDtos = new ArrayList<>();
        for (RcRunningLog runningLog : rcRunningLogs) {
            RunningLogDto logDto = new RunningLogDto();
            BeanUtils.copyProperties(runningLog, logDto);
            logDto.setDetail(JSONObject.parseArray(JSON.toJSONString(runningLog.getDetail()), RunningLogContentDto.class));
            logDtos.add(logDto);
        }
        return logDtos;
    }
    
    public static List<RunningLogHomePageVO> getRunningLogHomePageVO(List<RcRunningLog> rcRunningLogs) {
        List<RunningLogHomePageVO> logDtos = new ArrayList<>();
        
        if (CollectionUtil.isEmpty(rcRunningLogs)) {
            return logDtos;
        }
        
        for (RcRunningLog runningLog : rcRunningLogs) {
            RunningLogHomePageVO logDto = new RunningLogHomePageVO();
            String name = runningLog.getFirstLevelCategory() == null ? null :
                    runningLog.getFirstLevelCategory().getName();
            logDto.setFirstLevelCategoryName(name);
            if (runningLog.getContent() != null) {
                logDto.setContentDesc(runningLog.getContent().getContentDesc());
            }
            logDto.setCreateUser(runningLog.getCreateUser());
            logDto.setCreateTime(runningLog.getCreateTime());
            logDtos.add(logDto);
        }
        return logDtos;
    }
}
