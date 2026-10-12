package com.persagy.apccontrol.basic.model.enumtype.security;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 摄像头的类型以及说明
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/7 16:57
 * @since v5.240801.0
 */
@Getter
@AllArgsConstructor
public enum CameraEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 枪机摄像头 cameraType
     */
    GUN_CAMERA("1", "key_c0751131d00540028fe41505cdd552ea"),
    /**
     * 球机摄像头 cameraType
     */
    BALL_CAMERA("2", "key_568212fde3f545e29b2448c84418fe64"),
    /**
     * 半球机摄像头 cameraType
     */
    HEMISPHERICAL_CAMERA("3", "key_6708903003914e7a92a8fe4faa2ff5cb"),
    /**
     * 具有撤布防能力的摄像头 hasArmingFunc
     */
    CAMERAS_WITH_REMOVAL_CAPABILITY("1", "key_61d8f2dcd7d04621aa9d951b229692f1");

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
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }


    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AccessDemandStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, AccessDemandStatusEnum.class);
    }

    public static AccessDemandStatusEnum objectToEnum(Object value) {
        return analyze(String.valueOf(value));
    }

}
