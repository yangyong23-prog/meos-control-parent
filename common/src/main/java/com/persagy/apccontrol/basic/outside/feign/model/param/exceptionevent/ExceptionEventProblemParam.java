package com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent;

import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionEventProblemParam {

    @NonNull
    private Set<String> rwdObjectIds;

    @Nullable
    private Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2;
}
