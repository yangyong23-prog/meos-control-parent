package com.persagy.apccontrol.basic.controller.cfg.excel;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.entity.InfoCodeConfigExtendInfo;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.info.InfoTypeEnum;
import com.persagy.apccontrol.basic.resource.RcInfoCodeCfgService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/28 15:04
 */
@Slf4j
public class InfoCodeConfigExcelListener extends AnalysisEventListener<InfoCodeConfigExcel> {

    private RcInfoCodeCfgService infoCodeCfgService;

    private Map<String, RcInfoCodeCfg> firstGroupMap = new HashMap<>();
    private Map<String, RcInfoCodeCfg> secondGroupMap = new HashMap<>();

    private List<RcInfoCodeCfg> dataList = new ArrayList<>();
    private List<InfoCodeConfigExcel> excelDataList = new ArrayList<>();

    private List<CellExtra> cellExtraList = new ArrayList<>();

    private Map<Integer, String> headMap = new HashMap<>();
    private Map<String, List<RcInfoCodeCfg>> mergeCode_infoCode = new HashMap<>();

    public InfoCodeConfigExcelListener(RcInfoCodeCfgService infoCodeCfgService) {
        this.infoCodeCfgService = infoCodeCfgService;
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        switch (extra.getType()) {
            case COMMENT:
                break;
            case HYPERLINK:
                break;
            case MERGE:
                cellExtraList.add(extra);
                break;
            default:
        }
    }

    @Override
    public void invoke(InfoCodeConfigExcel configExcel, AnalysisContext context) {
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
            if (!StrUtil.isAllNotBlank(className, classCode, configExcel.getInfoCode())) {
                return;
            }
            if (classCode.matches("[a-zA-Z]+") && configExcel.getInfoCode().matches("[0-9a-zA-Z]+")) {
                configExcel.setClassCode(classCode);
                configExcel.setClassName(className);
                excelDataList.add(configExcel);
            }
        } catch (Exception e) {
            log.error("sheetName:{}, data: {}", sheetName, configExcel, e);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {
            // 1.优先处理合并单元格的数据
            this.mergeExcelData();

            // 2.数据格式转换
            for (InfoCodeConfigExcel configExcel : excelDataList) {
                this.transferInfoCodeConfig(configExcel);
            }

            // 3.扩展信息兼容处理
            this.wirteExtendInfo();

            // 4.数据入库
            if (!firstGroupMap.isEmpty()) {
                this.infoCodeCfgService.saveOrUpdateBatch(firstGroupMap.values());
            }
            if (!secondGroupMap.isEmpty()) {
                this.infoCodeCfgService.saveOrUpdateBatch(secondGroupMap.values());
            }
            if (!dataList.isEmpty()) {
                List<List<RcInfoCodeCfg>> partition = Lists.partition(dataList, 500);
                for (List<RcInfoCodeCfg> infoCodeConfigs : partition) {
                    this.infoCodeCfgService.saveOrUpdateBatch(infoCodeConfigs);
                }
            }

            // 5.聚合信息点数据更新
            if (!mergeCode_infoCode.isEmpty()) {
                List<RcInfoCodeCfg> updateList = new ArrayList<>();
                for (List<RcInfoCodeCfg> infoCodeConfigPos : mergeCode_infoCode.values()) {
                    RcInfoCodeCfg updateInfo = infoCodeConfigPos.get(0);
                    InfoCodeConfigExtendInfo extendInfo = updateInfo.getExtendInfo() == null ? new InfoCodeConfigExtendInfo() : updateInfo.getExtendInfo();
                    extendInfo.setMergeInfoCodes(Lists.newArrayList());
                    updateInfo.setExtendInfo(extendInfo);
                    infoCodeConfigPos.forEach(item -> updateInfo.getExtendInfo().getMergeInfoCodes().add(item.getInfoCode()));
                    updateList.add(updateInfo);
                }
                this.infoCodeCfgService.updateBatchById(updateList);
            }
        } catch (Exception e) {
            log.error("数据入库失败，异常数据: {}", excelDataList, e);
        } finally {
            cellExtraList.clear();
            firstGroupMap.clear();
            secondGroupMap.clear();
            excelDataList.clear();
            dataList.clear();
            mergeCode_infoCode.clear();
        }
    }

    /**
     * 对象转换
     *
     * @param configExcel
     * @return
     */
    private void transferInfoCodeConfig(InfoCodeConfigExcel configExcel) {
        if (!StrUtil.isAllNotBlank(configExcel.getInfoCode(), configExcel.getFirstGroup(), configExcel.getFirstTag())) {
            log.warn("信息点配置异常，异常数据：{}", configExcel);
            return;
        }
        String classCode = configExcel.getClassCode();

        // 记录一级分组
        String firstGroupId = RcInfoCodeCfg.getFirstGroupIdByName(classCode, configExcel.getFirstGroup());
        if (StrUtil.isNotBlank(firstGroupId) && !firstGroupMap.containsKey(firstGroupId)) {
            RcInfoCodeCfg group = new RcInfoCodeCfg();
            group.setId(firstGroupId);
            group.setDataType(InfoTypeEnum.GROUP);
            group.setName(configExcel.getFirstGroup());
            group.setOrdered(this.convertStr2Int(classCode, configExcel.getInfoCode(), configExcel.getFirstOrder()));
            group.setUpdateTime(LocalDateTime.now());
            firstGroupMap.put(firstGroupId, group);
        }
        // 记录二级分组
        String secondGroupId = StrUtil.isBlank(configExcel.getSecondGroup()) ? null : (classCode + MD5Utils.md5Hex(configExcel.getFirstGroup() + configExcel.getSecondGroup(), StandardCharsets.UTF_8.name()));
        if (StrUtil.isNotBlank(secondGroupId) && !secondGroupMap.containsKey(secondGroupId)) {
            RcInfoCodeCfg group = new RcInfoCodeCfg();
            group.setId(secondGroupId);
            group.setGroupInfo(firstGroupId);
            group.setDataType(InfoTypeEnum.GROUP);
            group.setName(configExcel.getSecondGroup());
            group.setOrdered(this.convertStr2Int(classCode, configExcel.getInfoCode(), configExcel.getSecondOrder()));
            group.setUpdateTime(LocalDateTime.now());
            secondGroupMap.put(secondGroupId, group);
        }

        // 记录信息点配置
        RcInfoCodeCfg infoCodeConfig = new RcInfoCodeCfg();
        BeanUtil.copyProperties(configExcel, infoCodeConfig, "scale");

        // 判断当前信息点是否属于首页 hover信息点，扩展信息与 wirteExtendInfo 中得更新，保持一致
        InfoCodeConfigExtendInfo extendInfo = new InfoCodeConfigExtendInfo();
        extendInfo.setImportanceLevel(configExcel.getImportanceLevel());
        if ("N".equalsIgnoreCase(configExcel.getDisplay())) {
            extendInfo.setView("-1");
        } else {
            extendInfo.setView("1");
        }

        // 判断是否需要进行聚合
        if (StrUtil.isNotBlank(configExcel.getMergeCode())) {
            mergeCode_infoCode.computeIfAbsent(configExcel.getMergeCode(), e -> Lists.newArrayList()).add(infoCodeConfig);
        }

        if ("orderFailAlarm".equals(infoCodeConfig.getInfoCode())) {
            List<DtpKnowledgeInfoCodeDataSourceDTO> dataSource = Lists.newArrayList(new DtpKnowledgeInfoCodeDataSourceDTO("0", LocaleMessageUtil.getMessage("key_e9d200d236e64b8584fb926b8aadddc4")), new DtpKnowledgeInfoCodeDataSourceDTO("1", LocaleMessageUtil.getMessage("key_892533af48e14d09aa565dac4095226d")));
            infoCodeConfig.setDataSource(dataSource);
        }
        infoCodeConfig.setExtendInfo(extendInfo);
        infoCodeConfig.setGroupInfo(StrUtil.isNotBlank(secondGroupId) ? secondGroupId : firstGroupId);
        infoCodeConfig.setId(classCode + configExcel.getInfoCode());
        infoCodeConfig.setOrdered(this.convertStr2Int(classCode, configExcel.getInfoCode(), configExcel.getInfoOrder()));
        infoCodeConfig.setScale(StrUtil.isBlank(configExcel.getScale()) ? null : new BigDecimal(configExcel.getScale()));
        infoCodeConfig.setUpdateTime(LocalDateTime.now());
        dataList.add(infoCodeConfig);
    }

    /**
     * 信息点配置扩展信息处理
     */
    private void wirteExtendInfo() {
        if (dataList.size() == 0) {
            return;
        }
        String classCode = dataList.get(0).getClassCode();

        List<RcInfoCodeCfg> oldInfoCodeConfigPos = this.infoCodeCfgService.queryAllInfoCodeConfigs(classCode);
        if (CollectionUtil.isEmpty(oldInfoCodeConfigPos)) {
            return;
        }

        // 扩展信息重新赋值
        Map<String, RcInfoCodeCfg> oldInfoCodeConfigPoMap = oldInfoCodeConfigPos.stream().filter(item -> !item.getId().startsWith("CONTROL"))
                .collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v2));
        for (RcInfoCodeCfg infoCodeConfigPo : dataList) {
            RcInfoCodeCfg oldConfigPo = oldInfoCodeConfigPoMap.get(infoCodeConfigPo.getInfoCode());
            if (oldConfigPo == null || oldConfigPo.getExtendInfo() == null) {
                continue;
            }
            if (infoCodeConfigPo.getExtendInfo() == null) {
                oldConfigPo.getExtendInfo().setView(null);
                oldConfigPo.getExtendInfo().setImportanceLevel(null);
            } else {
                oldConfigPo.getExtendInfo().setView(infoCodeConfigPo.getExtendInfo().getView());
                oldConfigPo.getExtendInfo().setImportanceLevel(infoCodeConfigPo.getExtendInfo().getImportanceLevel());
            }
            infoCodeConfigPo.setExtendInfo(oldConfigPo.getExtendInfo());
        }

    }

    /**
     * 单元格数据处理
     *
     */
    private void mergeExcelData() {
        cellExtraList.forEach(cellExtra -> {
            int firstRowIndex = cellExtra.getFirstRowIndex();
            int lastRowIndex = cellExtra.getLastRowIndex();
            int firstColumnIndex = cellExtra.getFirstColumnIndex();
            int lastColumnIndex = cellExtra.getLastColumnIndex();

            String headName = this.headMap.get(firstColumnIndex);
            if (StrUtil.isBlank(headName)) {
                return;
            }

            //获取初始值
            Object initValue = getInitValueFromList(headName, firstRowIndex, firstColumnIndex, excelDataList);
            if (initValue == null) {
                return;
            }

            //设置值
            for (int i = firstRowIndex; i <= lastRowIndex; i++) {
                for (int j = firstColumnIndex; j <= lastColumnIndex; j++) {
                    setInitValueToList(headName, initValue, i, j, excelDataList);
                }
            }
        });
    }

    /**
     * 赋值合并单元格的数据
     *
     * @param headName
     * @param filedValue
     * @param rowIndex
     * @param columnIndex
     * @param data
     */
    private void setInitValueToList(String headName, Object filedValue, Integer rowIndex, Integer columnIndex, List<InfoCodeConfigExcel> data) {
        InfoCodeConfigExcel object = data.get(rowIndex - 1);

        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null && annotation.value() != null && annotation.value().length > 0) {
                if (headName.equals(annotation.value()[0])) {
                    try {
                        field.set(object, filedValue);
                        break;
                    } catch (IllegalAccessException e) {
                        log.error("rowIndex: {}, columsIndex: {}, value error", rowIndex, columnIndex);
                    }
                }
            }
        }
    }

    /**
     * 获取合并单元格的数据
     *
     * @param headName
     * @param firstRowIndex
     * @param firstColumnIndex
     * @param data
     * @return
     */
    private Object getInitValueFromList(String headName, Integer firstRowIndex, Integer firstColumnIndex, List<InfoCodeConfigExcel> data) {
        Object filedValue = null;

        InfoCodeConfigExcel object = data.get(firstRowIndex - 1);
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null && annotation.value() != null && annotation.value().length > 0) {
                if (headName.equals(annotation.value()[0])) {
                    try {
                        filedValue = field.get(object);
                        break;
                    } catch (IllegalAccessException e) {
                        log.error("rowIndex: {}, columsIndex: {}, value error", firstRowIndex, firstColumnIndex);
                    }
                }
            }
        }
        return filedValue;
    }

    /**
     * 异常数据记录
     *
     * @param classCode
     * @param infoCode
     * @param valueStr
     * @return
     */
    private Integer convertStr2Int(String classCode, String infoCode, String valueStr) {
        try {
            if (StrUtil.isNotBlank(valueStr)) {
                return Integer.valueOf(valueStr);
            }
        } catch (Exception e) {
            log.info("配置显示顺序参数异常，classCode: {}, infoCode: {}", classCode, infoCode);
        }
        return 0;
    }

}
