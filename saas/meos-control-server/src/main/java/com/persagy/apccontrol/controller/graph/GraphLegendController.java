package com.persagy.apccontrol.controller.graph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphLegendUpdateDto;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphLegendGuestVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphLegendVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphTypeTreeVo;
import com.persagy.apccontrol.basic.service.systemgraph.GraphLegendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author houyuanzhi
 * @date 2023/6/25 14:59
 */
@RestController
@RequestMapping("/graph/legend")
@Slf4j
@Api(value = "资源库")
public class GraphLegendController {
    @Autowired
    private GraphLegendService graphLegendService;

    @ApiOperation("查询设备类树")
    @GetMapping("/type/tree")
    public List<GraphTypeTreeVo> typeTree() {
        return graphLegendService.typeTree();
    }

    @ApiOperation("查询图标")
    @GetMapping("/query/{classCode}")
    public List<GraphLegendVo> queryLegendList(@PathVariable String classCode) {
        return graphLegendService.queryLegendList(classCode);
    }

    @ApiOperation("查询图标(对外)")
    @GetMapping("/query/guest/{classCode}")
    public List<GraphLegendGuestVo> queryLegendListGuest(@PathVariable String classCode) {
        return graphLegendService.queryLegendListGuest(classCode);
    }

    @ApiOperation("查询所有图标")
    @GetMapping("/query/all")
    public List<GraphLegendVo> queryAll() {
        return graphLegendService.queryAllGraphLegend();
    }

    @ApiOperation("查询所有图标")
    @GetMapping("/initPicUrl")
    public Boolean initPicUrl() {
        return graphLegendService.initPicUrl();
    }

    @ApiOperation("图标编辑")
    @PostMapping("/update/{legendId}")
    public void updateLegend(@PathVariable String legendId, @RequestBody GraphLegendUpdateDto graphLegendVo) {
        graphLegendService.updateLegend(legendId, graphLegendVo);
    }

    @ApiOperation("图标删除")
    @GetMapping("/{legendId}")
    public void deleteLegend(@ApiParam("legendId") @PathVariable String legendId){
        graphLegendService.deleteByLegendId(legendId);
    }

    @ApiOperation("查询并更新图例及状态")
    @PostMapping("/save/all")
    public void saveAll() {
        graphLegendService.syncGraphLegend();
    }


}
