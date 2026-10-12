package com.persagy.apccontrol.basic.excel;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.entity.ClassPositionCodeConfig;
import com.persagy.apccontrol.basic.resource.ClassPositionCodeConfigService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 项目信息点管理范围配置excel导入
 *
 * @author YangWanYi
 * @version 1.0
 * @company persagy
 * @date 2022-08-11  15:29
 */
@Slf4j
public class ClassPositionCodeConfigExcelListener extends AnalysisEventListener<ClassPositionCodeConfigExcel> {

    private ClassPositionCodeConfigService classPositionCodeConfigService;

    private List<ClassPositionCodeConfig> dataList = new ArrayList<>();

    public ClassPositionCodeConfigExcelListener(ClassPositionCodeConfigService classPositionCodeConfigService) {
        this.classPositionCodeConfigService = classPositionCodeConfigService;
    }

    @Override
    public void invoke(ClassPositionCodeConfigExcel classPositionCodeConfigExcel, AnalysisContext analysisContext) {
        if (StrUtil.isBlank(classPositionCodeConfigExcel.getInfoCode())) {
            return;
        }
        ClassPositionCodeConfig classPositionCodeConfig = new ClassPositionCodeConfig();
        BeanUtils.copyProperties(classPositionCodeConfigExcel, classPositionCodeConfig);
        if (StrUtil.isBlank(classPositionCodeConfigExcel.getGroupCode())) {
            classPositionCodeConfig.setGroupCode("0");
        }
        if (StrUtil.isBlank(classPositionCodeConfigExcel.getProjectId())) {
            classPositionCodeConfig.setProjectId("0");
        }
        if (StrUtil.isBlank(classPositionCodeConfigExcel.getClassCode())) {
            classPositionCodeConfig.setClassCode("0");
        }
        classPositionCodeConfig.setId(classPositionCodeConfig.getGroupCode() + classPositionCodeConfig.getProjectId() + classPositionCodeConfig.getClassCode() + classPositionCodeConfig.getInfoCode());
        dataList.add(classPositionCodeConfig);
    }

    @SneakyThrows
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (dataList.size() > 0) {
            List<List<ClassPositionCodeConfig>> partition = Lists.partition(dataList, 500);
            for (List<ClassPositionCodeConfig> classPositionCodeConfigs : partition) {
                this.classPositionCodeConfigService.saveOrUpdateBatch(classPositionCodeConfigs);
            }
        }
        dataList.clear();
    }

}
