package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class OptTwinResponsesTest {

    @Test
    void nullResponseOrFailureCodeIsFailure() {
        Assertions.assertFalse(OptTwinResponses.succeeded(true, null));
        Assertions.assertFalse(OptTwinResponses.succeeded(false, Collections.singletonMap("respCode", "A0400")));
    }

    @Test
    void successCodeOrUnknownIsSuccess() {
        Assertions.assertTrue(OptTwinResponses.succeeded(false, Collections.singletonMap("respCode", "00000")));
        Assertions.assertTrue(OptTwinResponses.succeeded(false, null), "无 respCode 无法判断，按成功处理");
    }
}
