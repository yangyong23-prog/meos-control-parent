package com.persagy.apccontrol.controller.cfg.excel;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.entity.WebsocketConfig;
import com.persagy.apccontrol.basic.model.enumtype.FunctionTypeEnum;
import com.persagy.apccontrol.basic.resource.WebsocketConfigService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/28 15:04
 */
@Slf4j
public class WebsocketConfigExcelListener extends AnalysisEventListener<WebsocketConfigExcel> {

    private FunctionTypeEnum functionType;
    private WebsocketConfigService websocketConfigService;
    private List<WebsocketConfig> dataList = new ArrayList<>();

    public WebsocketConfigExcelListener(WebsocketConfigService websocketConfigService, FunctionTypeEnum functionType) {
        this.functionType = functionType;
        this.websocketConfigService = websocketConfigService;
    }

    @Override
    public void invoke(WebsocketConfigExcel configExcel, AnalysisContext context) {
        String sheetName = context.readSheetHolder().getSheetName();
        try {
            if (StrUtil.isBlank(sheetName)) {
                return;
            }
            String className = null;
            String classCode = null;
            try {
                String[] split = sheetName.split("\\(");
                className = split[0].trim();
                classCode = split[1];
                classCode = classCode.substring(0, classCode.length() - 1).trim();
            } catch (Exception e) {
                String[] split = sheetName.split("\\（");
                className = split[0].trim();
                classCode = split[1];
                classCode = classCode.substring(0, classCode.length() - 1).trim();
            }
            if (!StrUtil.isAllNotBlank(className, classCode)) {
                return;
            }

            configExcel.setClassCode(classCode);
            configExcel.setClassName(className);

            transferWebsocketConfig(configExcel);
        } catch (Exception e) {
            log.error("sheetName:{}, data: {}", sheetName, configExcel, e);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {
            if (dataList.size() > 0) {
                String classCode = dataList.get(0).getClassCode();
                LambdaQueryWrapper<WebsocketConfig> queryWrapper = new LambdaQueryWrapper<WebsocketConfig>().eq(WebsocketConfig::getFunctionType, functionType).eq(WebsocketConfig::getClassCode, classCode);
                this.websocketConfigService.remove(queryWrapper);
                List<List<WebsocketConfig>> partition = Lists.partition(dataList, 500);
                for (List<WebsocketConfig> websocketConfigs : partition) {
                    this.websocketConfigService.saveOrUpdateBatch(websocketConfigs);
                }
            }
        } catch (Exception e) {
            log.error("数据入库失败，异常数据: {}", dataList, e);
        } finally {
            dataList.clear();
        }
    }

    /**
     * 对象转换
     *
     * @param configExcel
     * @return
     */
    private void transferWebsocketConfig(WebsocketConfigExcel configExcel) {
        if (!StrUtil.isAllNotBlank(configExcel.getInfoCode(), configExcel.getClassCode())) {
            log.warn("信息点配置异常，异常数据：{}", configExcel);
            return;
        }

        WebsocketConfig websocketConfig = new WebsocketConfig();
        websocketConfig.setId(IdUtil.fastSimpleUUID());
        websocketConfig.setFunctionType(functionType);
        websocketConfig.setClassCode(configExcel.getClassCode());
        websocketConfig.setClassName(configExcel.getClassName());
        websocketConfig.setInfoCode(configExcel.getInfoCode());
        websocketConfig.setInfoName(configExcel.getInfoName());
        websocketConfig.setFirstTag(configExcel.getFirstTag());
        websocketConfig.setSecondTag(configExcel.getSecondTag());
        dataList.add(websocketConfig);
    }

}
