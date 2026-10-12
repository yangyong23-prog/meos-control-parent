package com.persagy.apccontrol.basic.service.system.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.QueryDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.system.SystemListDataService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author tangzy
 **/
@Slf4j
@Service
public class SystemListDataServiceImpl implements SystemListDataService {

    @Autowired
    private BizDtpRwdService bizDtpRwdService;

    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    /**
     * 查询系统列表数据
     *
     * @param params
     * @return
     */
    @Override
    public List<DtpRwdObjectDTO> querySystemListData(QueryDTO params) {
        return bizDtpRwdService.querySystemDiagramData(params);
    }

    @Override
    public List<DtpRwdObjectDTO> listSystemByClassCode(String classCode) {
        if (StringUtils.isBlank(classCode)) {
            return Collections.emptyList();
        }
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setClassCode(classCode);
        param.setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localId,DtpRwdObjectDTO.Fields.localName));
        return dtpRwdClientService.listObject(false, false, ContextUtil.getProjectId(), param);
    }


}