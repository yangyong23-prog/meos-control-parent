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
 * 数据字典-手自动状态设定
 *
 * <pre>{@code
 * {
 *   "version": "V5.5.73",
 *   "operate": -1,
 *   "ackState": 0,
 *   "ackBy": "0",
 *   "editedTime": 1701942270000,
 *   "editedMan": "任晓欣",
 *   "id": "824b788d590b11ed88910c42a131fc9f",
 *   "code": "manualAutoSet",
 *   "name": "手自动状态设定",
 *   "aliasCode": "manualAutoSet",
 *   "aliasName": "手自动状态设定",
 *   "classCode": "LTLTCC",
 *   "typeId": "802c005c56c54cb4b63ee1e2dbc01d20",
 *   "groupCode": "0",
 *   "projectId": "0",
 *   "category": "GRADATION",
 *   "firstTag": "设定参数",
 *   "inputMode": "L",
 *   "unit": "",
 *   "origDataType": "Boolp",
 *   "dataType": "BOOLEAN",
 *   "isMultiple": false,
 *   "isRegion": false,
 *   "dataSource": [
 *     {
 *       "code": "0",
 *       "name": "key_a2341d9fc6b04ba2a3f5781eb747b14c"
 *     },
 *     {
 *       "code": "1",
 *       "name": "key_e31ea221b6e74c5898d5ed6208968a54"
 *     }
 *   ],
 *   "remark": "",
 *   "showOrder": 20002020,
 *   "sourceFrom": 0,
 *   "appId": [
 *     "23"
 *   ]
 * }
 * }</pre>
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@InfoCode(InfoCodeEnum.RUN_STATUS)
public enum DtpKnowledgeManualAutoSetEnum implements BaseInfoCodeValueEnum<String>, IEnum<String> {

    /**
     * 手动
     */
    MANUAL("0", "key_a2341d9fc6b04ba2a3f5781eb747b14c"),

    /**
     * 自动
     */
    AUTO("1", "key_e31ea221b6e74c5898d5ed6208968a54");

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
    public static @Nullable DtpKnowledgeManualAutoSetEnum analyze(@Nullable Object value) {
        return BaseInfoCodeValueEnum.analyze(value, DtpKnowledgeManualAutoSetEnum.class);
    }
}
