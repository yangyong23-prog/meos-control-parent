package com.persagy.apccontrol.basic.excel;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.entity.RcObjectRelationCfg;
import com.persagy.apccontrol.basic.resource.RcObjectRelationCfgService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 项目关系对象查询配置excel导入
 *
 * @author YangWanYi
 * @version 1.0
 * @company persagy
 * @date 2022-08-11  15:29
 */
@Slf4j
public class RcObjectRelationCfgExcelListener extends AnalysisEventListener<RcObjectRelationCfgExcel> {

    private RcObjectRelationCfgService rcObjectRelationCfgService;

    private List<RcObjectRelationCfg> dataList = new ArrayList<>();

    private Map<String, String> oldId_newId = new HashMap<>();

    public RcObjectRelationCfgExcelListener(RcObjectRelationCfgService rcObjectRelationCfgService) {
        this.rcObjectRelationCfgService = rcObjectRelationCfgService;
    }

    @Override
    public void invoke(RcObjectRelationCfgExcel rcObjectRelationCfgExcel, AnalysisContext analysisContext) {
        RcObjectRelationCfg rcObjectRelationCfg = new RcObjectRelationCfg();
        BeanUtils.copyProperties(rcObjectRelationCfgExcel, rcObjectRelationCfg);
        if (StrUtil.isNotBlank(rcObjectRelationCfgExcel.getExtendInfo())) {
            rcObjectRelationCfg.setExtendInfo(JSONObject.parseObject(rcObjectRelationCfgExcel.getExtendInfo()));
        }
        // ID转换记录
        this.oldId_newId.put(rcObjectRelationCfg.getId(), rcObjectRelationCfg.getId() + rcObjectRelationCfg.getProjectId().substring(2));
        dataList.add(rcObjectRelationCfg);
    }

    @SneakyThrows
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (dataList.size() > 0) {
            // 先进行ID转换
            for (RcObjectRelationCfg rcObjectRelationCfg : dataList) {
                // 新ID
                rcObjectRelationCfg.setId(this.oldId_newId.get(rcObjectRelationCfg.getId()));
            }
            this.rcObjectRelationCfgService.saveOrUpdateBatch(dataList);
        }
        dataList.clear();
    }

}
