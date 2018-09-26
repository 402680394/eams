package com.ztdx.eams.controller.archives;

import com.ztdx.eams.domain.archives.model.PropertyType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/propertyType")
public class PropertyTypeController {

    /**
     * @api {get} /propertyType/list 获取字段属性列表
     * @apiName list
     * @apiGroup propertyType
     * @apiSuccess (Success 200) {Number} value 值.
     * @apiSuccess (Success 200) {String} name 名称.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {1: "档号",2: "题名"}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (PropertyType item : PropertyType.values()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("名称", item.getDescpriont());
            resultMap.put("值", item.getCode());
            resultList.add(resultMap);
        }
        return resultList;
    }
}
