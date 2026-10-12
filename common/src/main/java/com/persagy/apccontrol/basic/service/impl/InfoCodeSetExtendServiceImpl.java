package com.persagy.apccontrol.basic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.mapper.InfoCodeSetExtendMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.InfoCodeSetExtend;
import com.persagy.apccontrol.basic.model.enumtype.ManualAutoSetEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.apccontrol.basic.service.InfoCodeSetExtendService;
import com.persagy.framework.common.conditions.query.QueryWrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yuecaipu
 */
@Service
public class InfoCodeSetExtendServiceImpl extends ServiceImpl<InfoCodeSetExtendMapper, InfoCodeSetExtend> implements InfoCodeSetExtendService {

    @Autowired
    private PaasDataTransferClientService paasDataTransferClientService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void saveOrUpdateWithObjectIdAndInfoCode(InfoCodeSetExtend infoCodeSetExtend) {
        // 更新条件
        QueryWrap<InfoCodeSetExtend> wrap = new QueryWrap<InfoCodeSetExtend>()
                .eq(InfoCodeSetExtend.COL_OBJ_ID, infoCodeSetExtend.getObjId())
                .eq(InfoCodeSetExtend.COL_INFO_CODE, infoCodeSetExtend.getInfoCode());

        // 保存
        saveOrUpdate(infoCodeSetExtend, wrap);

        List<InfoCodeSetExtend> data = lambdaQuery()
                .eq(InfoCodeSetExtend::getObjId, infoCodeSetExtend.getObjId())
                .eq(InfoCodeSetExtend::getInfoCode, infoCodeSetExtend.getInfoCode())
                .list();
        for (InfoCodeSetExtend dataItem : data) {
            paasDataTransferClientService.syncDbDataToCloud(
                InfoCodeSetExtend.TABLE_NAME, Sets.newHashSet(InfoCodeSetExtend.COL_EXTEND),
                    DataTypeEnum.EDIT, objectMapper.convertValue(dataItem, ObjectNode.class));
        }
    }

    @Override
    public List<InfoCodeSetExtend> listByObjectId(String objectId) {
        return lambdaQuery().eq(InfoCodeSetExtend::getObjId, objectId).list();
    }


    @Override
    public InfoCodeSetExtend getByObjectIdAndInfoCode(String objectId, String infoCode) {
        return lambdaQuery().eq(InfoCodeSetExtend::getObjId, objectId).eq(InfoCodeSetExtend::getInfoCode, infoCode)
                .last(CommonConst.LIMIT_ONE).one();
    }

    @Override
    public List<InfoCodeSetExtend> listByInfoCodeAndModel(String infoCode, ManualAutoSetEnum manualAutoSet) {
        return lambdaQuery()
                .eq(InfoCodeSetExtend::getInfoCode, infoCode)
                .apply("extend ->> '$.manualAutoSet' = '" + manualAutoSet.getCode() + "'").list();
    }
}
