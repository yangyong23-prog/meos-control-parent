package com.persagy.apccontrol.basic.outside.feign.model.response.energyresearch;

import lombok.Data;

import java.util.List;


/**
 * 能耗模型
 * {
 *      "id": "13",
 *      "itemCode": "EI1010201010101001",
 *      "itemName": "制冷主机",
 *      "parentItemCode": "EI10102010101001",
 *      "level": 6,
 *      "rwdId": "Vo990999000710db2b8b9ff540b3bd0fbec39fc66057",
 *      "treeSourceNodeChildren": null,
 *      "disabled": false,
 *      "itemTags": [
 *      {
 *          "id": "5",
 *          "code": "",
 *          "name": "冷站",
 *          "displayOrder": 5
 *      }
 *      ]
 * }
 */
@Data
public class EnergyItemResponse {

    private String id;
    private String itemCode;
    private String itemName;
    private String parentItemCode;
    private String energyModelId;
    private int level;
    private String rwdId;
    private String treeSourceNodeChildren;
    private boolean disabled;
    private List<ItemTags> itemTags;

    @Data
    public static class ItemTags {
        private String id;
        private String code;
        private String name;
        private int displayOrder;
    }
}
