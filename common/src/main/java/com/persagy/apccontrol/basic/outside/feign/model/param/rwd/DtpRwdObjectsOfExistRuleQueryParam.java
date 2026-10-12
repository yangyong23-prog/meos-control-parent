package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;

import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class DtpRwdObjectsOfExistRuleQueryParam {

    @Schema(description = "设备类编码")
    private String classCode;

    @Schema(description = "过滤信息点")
    private Set<String> infoCodes;

    public static DtpRwdObjectsOfExistRuleQueryParam of(@NotNull String classCode, @Nullable Set<String> infoCodes) {
        DtpRwdObjectsOfExistRuleQueryParam param = new DtpRwdObjectsOfExistRuleQueryParam();
        param.classCode = classCode;
        param.infoCodes = infoCodes;
        return param;
    }

    public static DtpRwdObjectsOfExistRuleQueryParam of(@NotNull String classCode) {
        DtpRwdObjectsOfExistRuleQueryParam param = new DtpRwdObjectsOfExistRuleQueryParam();
        param.classCode = classCode;
        return param;
    }

    public static List<DtpRwdObjectsOfExistRuleQueryParam> of(@NotNull List<ClassCode> classCodeList) {
        return classCodeList.stream().map(classCode ->
                DtpRwdObjectsOfExistRuleQueryParam.of(classCode.getClassCode(), classCode.getInfoCodes())).collect(Collectors.toList());
    }

    public static List<DtpRwdObjectsOfExistRuleQueryParam> of(@NotNull Collection<String> classCodeList) {
        return classCodeList.stream().map(DtpRwdObjectsOfExistRuleQueryParam::of).collect(Collectors.toList());
    }
}
