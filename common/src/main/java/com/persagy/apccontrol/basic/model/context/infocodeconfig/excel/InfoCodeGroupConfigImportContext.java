package com.persagy.apccontrol.basic.model.context.infocodeconfig.excel;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.utils.excel.ExcelUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * 信息点分组配置导入上下文
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@Accessors(chain = true)
public class InfoCodeGroupConfigImportContext implements LogComponent {

    // ------------------------- 参数 -------------------------

    /**
     * 分组编码
     */
    private String groupCode;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 展示样式
     */
    private JsonNode showStyle;

    /**
     * 分组顺序
     */
    private Integer sequence;

    /**
     * 上级分组编码
     */
    @Nullable
    private String parentGroupCode;

    /**
     * 子分组
     */
    private List<InfoCodeGroupConfigImportContext> children = Collections.emptyList();

    // ------------------------- 过程 -------------------------

    /**
     * 新增后的模版类型ID
     */
    private Long groupConfigIdAfterAdded;

    // ------------------------- 结果 -------------------------

    /**
     * 是否失败
     */
    private boolean failed = false;

    /**
     * 错误信息
     */
    private List<String> errorMessages = new ArrayList<>();

    /**
     * 构造器
     *
     * @param row 行
     * @since 5.240602.0
     */
    public InfoCodeGroupConfigImportContext(@NonNull Row row) {
        this.groupCode = ExcelUtils.analyzeValueByString(row, 1);
        if (StringUtils.isBlank(groupCode)) {
            ExcelUtils.validFailedLog(row, 1, LocaleMessageUtil.getMessage("key_9e2dcb4d4f3b4023ad2b608d7fa62336"), getErrorMessages());
            this.failed = true;
        }
        this.groupName = ExcelUtils.analyzeValueByString(row, 0);
        if (StringUtils.isBlank(groupName)) {
            ExcelUtils.validFailedLog(row, 0, LocaleMessageUtil.getMessage("key_6af47fa08bdf43a485eebff88c18ac16"), getErrorMessages());
            this.failed = true;
        }
        this.showStyle = ExcelUtils.analyzeValueByJsonNode(row, 5, e -> {
            ExcelUtils.validFailedLog(row, 5, LocaleMessageUtil.getMessage("key_cb247714f2a14fb6a9a458d4e1b1905f"), getErrorMessages());
            this.failed = true;
        });
        this.sequence = ExcelUtils.analyzeValueByInteger(row, 2);
        this.parentGroupCode = ExcelUtils.analyzeValueByString(row, 3);
    }

    // ------------------------- 行为 -------------------------

    /**
     * 创建配置上下文
     *
     * @param sheet Sheet
     * @return 配置上下文
     * @since 5.240602.0
     */
    public static List<InfoCodeGroupConfigImportContext> of(@NonNull Sheet sheet) {
        // 从第一行开始解析
        List<Row> rows = ExcelUtils.analyzeRows(sheet, 1);
        List<InfoCodeGroupConfigImportContext> result = new ArrayList<>();
        Set<String> groupCodes = new HashSet<>();
        for (Row row : rows) {
            InfoCodeGroupConfigImportContext context = new InfoCodeGroupConfigImportContext(row);
            if (context.isFailed()) {
                result.add(context);
                continue;
            }
            String groupCode = context.getGroupCode();
            if (groupCodes.contains(groupCode)) {
                throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_558ce46c22f04324983609bbc0e48b19"));
            }
            groupCodes.add(groupCode);
            result.add(context);
        }
        return result;
    }

    /**
     * 转换为新增信息点分组配置实体
     *
     * @param relParentId 关联父分组ID
     * @return 新增信息点分组配置实体
     * @since 5.240602.0
     */
    public InfoCodeGroup toAddEquipmentInfoCodeGroupConfig(@Nullable Long relParentId) {
        return SpringUtil.getBean(InfoCodeConverter.class).toEntity(this, relParentId);
    }

    @Override
    public String logTag() {
        return "信息点分组配置-导入上下文-配置";
    }
}
