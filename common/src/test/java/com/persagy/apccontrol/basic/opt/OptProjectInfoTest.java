package com.persagy.apccontrol.basic.opt;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class OptProjectInfoTest {

    private static JSONObject rwdProject(String name, String localName, String city) {
        JSONObject obj = new JSONObject();
        obj.put("id", "Pj1");
        obj.put("name", name);
        obj.put("localName", localName);
        JSONObject infos = new JSONObject();
        infos.put("city", city);
        obj.put("infos", infos);
        return obj;
    }

    private static ProjectDTO remoteProject() {
        ProjectDTO p = new ProjectDTO();
        p.setProjectId("Pj1");
        p.setProjectName("远程名");
        p.setProjectLocalName("远程名");
        p.setCityCode("110100");
        p.setProvinceName("北京");
        return p;
    }

    @Test
    void mapsLocalNameFirstThenNameAndInfosCity() {
        ProjectDTO p = OptProjectInfo.fromRwd(rwdProject("全称", "本地名", "310100"), "G1", "Pj1");
        Assertions.assertEquals("本地名", p.getProjectName());
        Assertions.assertEquals("本地名", p.getProjectLocalName());
        Assertions.assertEquals("310100", p.getCityCode());
        Assertions.assertEquals("G1", p.getGroupCode());
        Assertions.assertEquals("Pj1", p.getProjectId());

        ProjectDTO q = OptProjectInfo.fromRwd(rwdProject("全称", " ", "310100"), "G1", "Pj1");
        Assertions.assertEquals("全称", q.getProjectLocalName());
    }

    @Test
    void missingCityOrNameMeansNotFound() {
        Assertions.assertNull(OptProjectInfo.fromRwd(rwdProject("全称", "本地名", ""), "G1", "Pj1"));
        Assertions.assertNull(OptProjectInfo.fromRwd(rwdProject("", null, "310100"), "G1", "Pj1"));
        Assertions.assertNull(OptProjectInfo.fromRwd(null, "G1", "Pj1"));
        JSONObject flat = new JSONObject();
        flat.put("localName", "本地名");
        flat.put("city", "320100");
        Assertions.assertEquals("320100", OptProjectInfo.fromRwd(flat, "G1", "Pj1").getCityCode());
    }

    @Test
    void readsRwdOnceWithinRefreshWindowAndNeverCallsRemote() {
        AtomicInteger rwdCalls = new AtomicInteger();
        AtomicInteger remoteCalls = new AtomicInteger();
        AtomicLong now = new AtomicLong(0);
        OptProjectInfo info = new OptProjectInfo((g, p) -> {
            rwdCalls.incrementAndGet();
            return rwdProject("全称", "本地名", "310100");
        }, now::get);
        Function<String, ProjectDTO> remote = id -> {
            remoteCalls.incrementAndGet();
            return remoteProject();
        };
        for (int i = 0; i < 5; i++) {
            Assertions.assertEquals("310100", info.get("G1", "Pj1", remote).getCityCode());
        }
        Assertions.assertEquals(1, rwdCalls.get());
        Assertions.assertEquals(0, remoteCalls.get());

        now.addAndGet(TimeUnit.MINUTES.toMillis(361));
        info.get("G1", "Pj1", remote);
        Assertions.assertEquals(2, rwdCalls.get());
        Assertions.assertEquals(0, remoteCalls.get());
    }

    @Test
    void fallsBackToRemoteOnceAndCachesIt() {
        AtomicInteger remoteCalls = new AtomicInteger();
        OptProjectInfo info = new OptProjectInfo((g, p) -> {
            throw new IllegalStateException("rwd down");
        }, () -> 0L);
        Function<String, ProjectDTO> remote = id -> {
            remoteCalls.incrementAndGet();
            return remoteProject();
        };
        Assertions.assertEquals("110100", info.get("G1", "Pj1", remote).getCityCode());
        Assertions.assertEquals("北京", info.get("G1", "Pj1", remote).getProvinceName());
        Assertions.assertEquals(1, remoteCalls.get());
    }

    @Test
    void keepsOldValueWhenBothFailOnRefresh() {
        AtomicLong now = new AtomicLong(0);
        AtomicReference<JSONObject> rwd = new AtomicReference<>(rwdProject("全称", "本地名", "310100"));
        OptProjectInfo info = new OptProjectInfo((g, p) -> rwd.get(), now::get);
        info.get("G1", "Pj1", id -> null);

        rwd.set(null);
        now.addAndGet(TimeUnit.MINUTES.toMillis(400));
        ProjectDTO p = info.get("G1", "Pj1", id -> {
            throw new IllegalStateException("mid-permission down");
        });
        Assertions.assertEquals("310100", p.getCityCode());
    }

    @Test
    void remoteExceptionPropagatesWhenNothingCached() {
        OptProjectInfo info = new OptProjectInfo((g, p) -> null, () -> 0L);
        Assertions.assertThrows(IllegalStateException.class, () -> info.get("G1", "Pj1", id -> {
            throw new IllegalStateException("isThrows=true");
        }));
        Assertions.assertNull(info.get("G1", "Pj1", id -> null));
    }

    @Test
    void returnsCopiesSoCallersCannotCorruptTheCache() {
        OptProjectInfo info = new OptProjectInfo((g, p) -> rwdProject("全称", "本地名", "310100"), () -> 0L);
        info.get("G1", "Pj1", id -> null).setCityCode("changed");
        Assertions.assertEquals("310100", info.get("G1", "Pj1", id -> null).getCityCode());
    }

    @Test
    void decodesOperatorNameHeader() {
        Assertions.assertEquals("张三", OptOperatorNames.decode("%E5%BC%A0%E4%B8%89"));
        Assertions.assertEquals("a+b", OptOperatorNames.decode("a+b"));
        Assertions.assertEquals("100%", OptOperatorNames.decode("100%"));
        Assertions.assertNull(OptOperatorNames.decode("  "));
        Assertions.assertNull(OptOperatorNames.decode(null));
        Assertions.assertEquals(OptOperatorNames.MAX_NAME_LENGTH,
                OptOperatorNames.decode(new String(new char[100]).replace('\0', 'x')).length());
    }
}
