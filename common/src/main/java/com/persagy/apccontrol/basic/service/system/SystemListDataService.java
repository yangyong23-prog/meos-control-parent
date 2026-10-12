package com.persagy.apccontrol.basic.service.system;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.QueryDTO;

import java.util.List;

/**
 * @author tangzy
 **/
public interface SystemListDataService {


    /**
     * 查询系统列表数据
     *
     * @param params params
     * @return DtpRwdObjectDTO
     * @author tangzy
     **/
    List<DtpRwdObjectDTO> querySystemListData(QueryDTO params);

    List<DtpRwdObjectDTO> listSystemByClassCode(String classCode);
}
