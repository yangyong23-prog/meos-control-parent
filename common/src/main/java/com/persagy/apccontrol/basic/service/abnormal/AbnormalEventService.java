package com.persagy.apccontrol.basic.service.abnormal;

import com.persagy.apccontrol.basic.model.param.abnormal.AbnormalEventCommonParam;
import com.persagy.apccontrol.basic.model.param.abnormal.AbnormalEventQueryParam;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 异常事件通用查询
 *
 * @author XueTao
 * @date 2024/5/29 15:27
 * @since v5.240502.0
 */
public interface AbnormalEventService {

    /**
     * 获取异常事件列表
     *
     * @param abnormalEventQueryParam 异常接口
     * @return 异常事件数据
     */
    List<ExceptionEventBaseInfoVO> listAbnormalEvent(AbnormalEventQueryParam abnormalEventQueryParam);

    List<ExceptionEventBaseInfoVO> listAbnormalEventNew(AbnormalEventQueryParam abnormalEventQueryParam);

    /**
     * 查询空间带设备异常信息列表
     *
     * @param param 查询参数
     * @return 空间带设备异常信息列表
     * @since 5.241101.0
     */
    List<ExceptionEventBaseInfoVO> listAbnormalEventBySpaceWithEquipments(@NonNull @Valid AbnormalEventQueryParam param);

    /**
     * 查询-按系统划分异常事件
     *
     * @param abnormalEventQueryParam 按系统划分异常事件入参
     * @return 异常事件列表
     */
    List<ExceptionEventBaseInfoVO> getAbnormalDataListDivideSystem(AbnormalEventCommonParam abnormalEventQueryParam);

    /**
     * 查询-根据入参的系统二级运行监测类型查询对象的异常事件
     *
     * @param abnormalDataQueryParam 对象id集合+二级运行检测类型
     * @return <对象id,异常事件列表>
     */
    Map<String, List<ExceptionEventBaseInfoVO>> mapObjectIdAndAbnormalEventWithConditions(
            @NonNull AbnormalEventQueryParam abnormalDataQueryParam);

    /**
     * 统计-根据入参的系统二级运行监测类型查询哪些对象有异常事件
     *
     * @param abnormalDataQueryParam 对象id集合+二级运行检测类型
     * @return 对象id集合
     */
    Set<String> countAbnormalEvent(@NonNull AbnormalEventQueryParam abnormalDataQueryParam);

}
