package com.persagy.apccontrol.basic.model.enumtype.dtpknowledge;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.config.annotation.InfoCode;
import com.persagy.apccontrol.basic.model.base.BaseInfoCodeValueEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * 数据字典-控制偏差状态
 *
 * <pre>{@code
 * {
 *     "version": "V5.5.60",
 *     "operate": -1,
 *     "ackState": 0,
 *     "editedTime": 1744183788000,
 *     "editedMan": "张环宇",
 *     "id": "67ea555be4b05f1259ed1399",
 *     "code": "controlDeviationStatus",
 *     "name": "控制偏差状态",
 *     "aliasCode": "controlDeviationStatus",
 *     "aliasName": "控制偏差状态",
 *     "classCode": "LTLTCC",
 *     "typeId": "802c005c56c54cb4b63ee1e2dbc01d20",
 *     "groupCode": "0",
 *     "projectId": "0",
 *     "category": "SEQUENTIAL",
 *     "firstTag": "运行参数",
 *     "secondTag": "",
 *     "inputMode": "L",
 *     "unit": "",
 *     "origDataType": "Enumv",
 *     "dataType": "ENUM",
 *     "isMultiple": false,
 *     "isRegion": false,
 *     "dataSource": [
 *         {
 *             "code": "1",
 *             "name": "key_a47ebf0adb8746798241d4ca75d4c82b",
 *             "name2": "Over-Opening"
 *         },
 *         {
 *             "code": "2",
 *             "name": "key_bafc5219c3474688ac414c801e4ed335",
 *             "name2": "Under-Opening"
 *         },
 *         {
 *             "code": "3",
 *             "name": "key_004d8bd0d1714f878918ca260e927c2e",
 *             "name2": "No Deviation"
 *         }
 *     ],
 *     "timeConst": "15分钟",
 *     "showOrder": 20002100,
 *     "sourceFrom": 0
 * }
 * }</pre>
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@AllArgsConstructor
@InfoCode(InfoCodeEnum.CONTROL_DEVIATION_STATUS)
public enum DtpKnowledgeControlDeviationStatusEnum implements BaseInfoCodeValueEnum<String>, IEnum<String> {

    /**
     * 多开
     */
    OVER_OPENING("1", "key_a47ebf0adb8746798241d4ca75d4c82b"),

    /**
     * 少开
     */
    UNDER_OPENING("2", "key_bafc5219c3474688ac414c801e4ed335"),

    /**
     * 无偏差
     */
    NO_DEVIATION("3", "key_004d8bd0d1714f878918ca260e927c2e");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static @Nullable DtpKnowledgeControlDeviationStatusEnum analyze(@Nullable Object value) {
        return BaseInfoCodeValueEnum.analyze(value, DtpKnowledgeControlDeviationStatusEnum.class);
    }
}
