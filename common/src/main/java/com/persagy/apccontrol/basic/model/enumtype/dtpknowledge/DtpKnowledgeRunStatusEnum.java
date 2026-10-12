package com.persagy.apccontrol.basic.model.enumtype.dtpknowledge;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.config.annotation.InfoCode;
import com.persagy.apccontrol.basic.model.base.BaseInfoCodeValueEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * 数据字典-运行状态
 *
 * <pre>{@code
 * {
 *     "version": "V5.5.61",
 *     "operate": -1,
 *     "ackState": 0,
 *     "ackBy": "0",
 *     "editedTime": 1701942571000,
 *     "editedMan": "孟凡露",
 *     "id": "824b7fb6590b11ed88910c42a131fc9f",
 *     "code": "runStatus",
 *     "name": "运行状态",
 *     "aliasCode": "runStatus",
 *     "aliasName": "运行状态",
 *     "classCode": "LTLTCC",
 *     "typeId": "802c005c56c54cb4b63ee1e2dbc01d20",
 *     "groupCode": "0",
 *     "projectId": "0",
 *     "category": "SEQUENTIAL",
 *     "firstTag": "运行参数",
 *     "inputMode": "L",
 *     "unit": "",
 *     "origDataType": "Boolv",
 *     "dataType": "BOOLEAN",
 *     "isMultiple": false,
 *     "isRegion": false,
 *     "dataSource": [
 *         {
 *             "code": "0",
 *             "name": "key_7579eee332eb44e5a71aaea5d7d11957"
 *         },
 *         {
 *             "code": "1",
 *             "name": "key_e347f04da74d4b12a28617d8b450cd0c"
 *         }
 *     ],
 *     "remark": "",
 *     "showOrder": 16002005,
 *     "sourceFrom": 0,
 *     "appId": [
 *         "23"
 *     ]
 * }
 * }</pre>
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@InfoCode(InfoCodeEnum.RUN_STATUS)
public enum DtpKnowledgeRunStatusEnum implements BaseInfoCodeValueEnum<String>, IEnum<String> {

    /**
     * 多开
     */
    STOP("0", "key_7579eee332eb44e5a71aaea5d7d11957"),

    /**
     * 运行
     */
    RUNNING("1", "key_e347f04da74d4b12a28617d8b450cd0c");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public String value() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static @Nullable DtpKnowledgeRunStatusEnum analyze(@Nullable Object value) {
        return BaseInfoCodeValueEnum.analyze(value, DtpKnowledgeRunStatusEnum.class);
    }
}
