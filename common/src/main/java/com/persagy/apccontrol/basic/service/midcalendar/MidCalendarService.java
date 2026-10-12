package com.persagy.apccontrol.basic.service.midcalendar;

import com.persagy.apccontrol.basic.model.param.midcalendar.MidCalendarWorkTimeBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.midcalendar.MidCalendarWorkTimeVO;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 工作历服务
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
public interface MidCalendarService {

    /**
     * 查询营业时间
     *
     * @param param 查询参数
     * @return 营业时间
     * @since 7.250702.0
     */
    List<MidCalendarWorkTimeVO> listWorkTimes(@NonNull @Valid MidCalendarWorkTimeBatchQueryParam param);

}
