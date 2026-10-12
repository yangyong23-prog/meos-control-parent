package com.persagy.apccontrol.basic.model.dto.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.CoordinateDto;
import lombok.Data;

import java.util.Objects;

/**
 * 管道节点
 * @author houyuanzhi
 * @date 2023/6/28 17:11
 */
@Data
public class LineNode {

    /**
     * 节点坐标
     */
    private CoordinateDto coordinateDto;

    /**
     * 管道标记（上）
     */
    private boolean lineMarkUp = false;

    /**
     * 管道线条样式（上）
     */
    private String pipelineStyleCodeUp = "line00";

    /**
     * 管道标记（下）
     */
    private boolean lineMarkDown = false;

    /**
     * 管道线条样式（下）
     */
    private String pipelineStyleCodeDown = "line00";

    /**
     * 管道标记（左）
     */
    private boolean lineMarkLeft = false;

    /**
     * 管道线条样式（左）
     */
    private String pipelineStyleCodeLeft = "line00";

    /**
     * 管道标记（右）
     */
    private boolean lineMarkRight = false;

    /**
     * 管道线条样式（右）
     */
    private String pipelineStyleCodeRight = "line00";


    /**
     * 筛选去掉不合格节点
     * @return
     */
    public boolean filterLineNode(){
        //只有上下方向有管道
        if (lineMarkUp && lineMarkDown && !lineMarkLeft && !lineMarkRight){
            return false;
        }
        //只有左右方向有管道
        if (!lineMarkUp && !lineMarkDown && lineMarkLeft && lineMarkRight){
            return false;
        }
        int count = 0;
        if (lineMarkUp){
            count++;
        }
        if (lineMarkDown){
            count++;
        }
        if (lineMarkLeft){
            count++;
        }
        if (lineMarkRight){
            count++;
        }
        return count != 0 && count != 1;
    }

    /**
     * 标记节点向量，Y坐标
     */
    public void changeLineMarkY(Integer nowY, Integer lastY, String pipelineStyleCode){
        //节点左边在两者之间，标记上下向量
        Integer nodeY = this.getCoordinateDto().getY();
        if (isBetween(nowY,lastY,nodeY)){
            lineMarkUp = true;
            pipelineStyleCodeUp = pipelineStyleCode;
            lineMarkDown = true;
            pipelineStyleCodeDown = pipelineStyleCode;
        }

        //节点在端点上，标记一个方向的向量
        if (Objects.equals(nowY,nodeY)){
            if (nowY > lastY){
                lineMarkUp = true;
                pipelineStyleCodeUp = pipelineStyleCode;
            } else {
                lineMarkDown = true;
                pipelineStyleCodeDown = pipelineStyleCode;
            }
        } else if (Objects.equals(lastY,nodeY)){
            if (nowY > lastY){
                lineMarkDown = true;
                pipelineStyleCodeDown = pipelineStyleCode;
            } else {
                lineMarkUp = true;
                pipelineStyleCodeUp = pipelineStyleCode;
            }
        }
    }


    /**
     * 标记节点向量，X坐标
     */
    public void changeLineMarkX(Integer nowX, Integer lastX, String pipelineStyleCode){
        //节点左边在两者之间，标记左右向量
        Integer nodeX = this.getCoordinateDto().getX();
        if (isBetween(nowX,lastX,nodeX)){
            lineMarkLeft = true;
            pipelineStyleCodeLeft = pipelineStyleCode;
            lineMarkRight = true;
            pipelineStyleCodeRight = pipelineStyleCode;
        }

        //节点在端点上，标记一个方向的向量
        if (Objects.equals(nowX,nodeX)){
            if (nowX > lastX){
                lineMarkLeft = true;
                pipelineStyleCodeLeft = pipelineStyleCode;
            } else {
                lineMarkRight = true;
                pipelineStyleCodeRight = pipelineStyleCode;
            }
        } else if (Objects.equals(lastX,nodeX)){
            if (nowX > lastX){
                lineMarkRight = true;
                pipelineStyleCodeRight = pipelineStyleCode;
            } else {
                lineMarkLeft = true;
                pipelineStyleCodeLeft = pipelineStyleCode;
            }
        }
    }

    /**
     * 判断节点某个坐标是否在两者之间，不能在端点上
     * @param now
     * @param last
     * @param node
     * @return
     */
    private boolean isBetween(Integer now, Integer last, Integer node){
        if (now > last){
            return node < now && node >last;
        } else if (now < last){
            return node > now && node < last;
        } else {
            return false;
        }
    }
}
