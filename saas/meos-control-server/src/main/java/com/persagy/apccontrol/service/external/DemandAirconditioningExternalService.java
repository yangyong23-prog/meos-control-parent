package com.persagy.apccontrol.service.external;

import com.persagy.apccontrol.basic.model.param.demand.airconditioning.DemandAirconditioningObjectQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.airconditioning.DemandAirconditioningObjectVO;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 需求管理外部服务
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>listControlledObject</b> - 查询管控对象列表</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface DemandAirconditioningExternalService {

    /**
     * 查询管控对象
     *
     * @param param 查询参数
     * @return 管控对象
     * @since v5.7.0
     */
    List<DemandAirconditioningObjectVO> listControlledObject(
            @NonNull @Valid DemandAirconditioningObjectQueryParam param);

}
