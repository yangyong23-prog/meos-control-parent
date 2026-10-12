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
 * 数据字典-本地远程状态
 *
 * <pre>{@code
 * {
 *   "version": "V5.5.84",
 *   "operate": -1,
 *   "ackState": 0,
 *   "ackBy": "0",
 *   "editedTime": 1701942449000,
 *   "editedMan": "任晓欣",
 *   "id": "82096ff5590b11ed88910c42a131fc9f",
 *   "code": "localRemoteStatus",
 *   "name": "本地远程状态",
 *   "aliasCode": "localRemoteStatus",
 *   "aliasName": "本地远程状态",
 *   "classCode": "ACCCCC",
 *   "typeId": "27893b10d56d43a98a8c80fcd4ef1358",
 *   "groupCode": "0",
 *   "projectId": "0",
 *   "category": "SEQUENTIAL",
 *   "firstTag": "运行参数",
 *   "priority": "重要",
 *   "inputMode": "L",
 *   "origDataType": "Boolv",
 *   "dataType": "BOOLEAN",
 *   "isMultiple": false,
 *   "isRegion": false,
 *   "dataSource": [
 *     {
 *       "code": "0",
 *       "name": "本地"
 *     },
 *     {
 *       "code": "1",
 *       "name": "远程"
 *     }
 *   ],
 *   "timeConst": "15分钟",
 *   "showOrder": 16002020,
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
@InfoCode(InfoCodeEnum.LOCAL_REMOTZE_STATUS)
public enum DtpKnowledgeLocalRemoteStatusEnum implements BaseInfoCodeValueEnum<String>, IEnum<String> {

    /**
     * 本地
     */
    LOCAL("0", "key_0496c23144324b83b80df317faf857d6"),

    /**
     * 远程
     */
    REMOTE("1", "key_34bb3f4a33064f4eab818f5b4f438d41");

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
    public static @Nullable DtpKnowledgeLocalRemoteStatusEnum analyze(@Nullable Object value) {
        return BaseInfoCodeValueEnum.analyze(value, DtpKnowledgeLocalRemoteStatusEnum.class);
    }
}
