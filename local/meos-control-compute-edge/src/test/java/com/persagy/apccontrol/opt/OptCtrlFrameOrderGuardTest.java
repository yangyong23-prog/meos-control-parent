package com.persagy.apccontrol.opt;

import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OptCtrlFrameOrderGuardTest {

    private static FeedbackInfoMsgMqttVO frame(String id, Integer status) {
        FeedbackInfoMsgMqttVO vo = new FeedbackInfoMsgMqttVO();
        vo.setId(id);
        vo.setDataStatus(status);
        return vo;
    }

    @Test
    void discardsWhenSameCommandAlreadyCompleted() {
        Assertions.assertTrue(OptCtrlFrameOrderGuard.isCompletedSameCommand(frame("c1", 2), frame("c1", 1)));
    }

    @Test
    void keepsWhenDifferentCommandOrNotCompleted() {
        Assertions.assertFalse(OptCtrlFrameOrderGuard.isCompletedSameCommand(frame("c1", 2), frame("c2", 1)));
        Assertions.assertFalse(OptCtrlFrameOrderGuard.isCompletedSameCommand(frame("c1", 1), frame("c1", 2)));
        Assertions.assertFalse(OptCtrlFrameOrderGuard.isCompletedSameCommand(frame(null, 2), frame(null, 1)));
        Assertions.assertFalse(OptCtrlFrameOrderGuard.isCompletedSameCommand(null, frame("c1", 1)));
    }
}
