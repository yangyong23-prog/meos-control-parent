package com.persagy.apccontrol.controller.graph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphIconDeleteDto;
import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphIconUpdateDto;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphIcon;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphIconVo;
import com.persagy.apccontrol.basic.service.systemgraph.GraphIconService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author houyuanzhi
 * @date 2023/6/25 14:59
 */
@RestController
@RequestMapping("/graph/icon")
@Slf4j
@Api(value = "图标库")
public class GraphIconController {

    @Autowired
    private GraphIconService graphIconService;

    @ApiOperation("查询图标列表")
    @GetMapping("/query/{classCode}")
    public List<GraphIconVo> queryLegendList(@PathVariable String classCode) {
        return graphIconService.queryLegendList(classCode);
    }

    @ApiOperation("查询所有图标")
    @GetMapping("/query/all")
    public List<GraphIconVo> queryAll() {
        return graphIconService.queryAllGraphIcon();
    }

    @ApiOperation("查询所有图标")
    @GetMapping("/initPicUrl")
    public Boolean initPicUrl() {
        return graphIconService.initPicUrl();
    }

    @ApiOperation("图标编辑")
    @PostMapping("/edit")
    public void updateLegend(@RequestBody GraphIconUpdateDto updateDto) {
        graphIconService.updateLegend(updateDto);
    }

    @ApiOperation("图标查询")
    @GetMapping("/query/{legendId}")
    public List<GraphIcon> getLegendByLegendId(@PathVariable String legendId) {
        return graphIconService.getLegendByLegendId(legendId);
    }

    @ApiOperation("图标删除")
    @PostMapping("/delete")
    public void deleteLegend(@RequestBody GraphIconDeleteDto deleteDto) {
        graphIconService.delete(deleteDto);
    }

    @ApiOperation("图标同步")
    @PostMapping("/save/sync")
    public void saveAll() {
        graphIconService.syncGraphIcon();
    }


}
