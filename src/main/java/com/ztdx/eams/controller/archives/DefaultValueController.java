package com.ztdx.eams.controller.archives;

import com.ztdx.eams.domain.archives.model.DefaultValue;
import com.ztdx.eams.domain.archives.model.PropertyType;
import org.jooq.types.UInteger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/defaultValue")
public class DefaultValueController {

    /**
     * @api {get} /defaultValue/list 获取默认值列表
     * @apiName list
     * @apiGroup defaultValue
     * @apiSuccess (Success 200) {Number} value 值.
     * @apiSuccess (Success 200) {String} name 名称.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"value": ID,"name": "名称"}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (DefaultValue item : DefaultValue.values()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("名称", item.getDescpriont());
            resultMap.put("值", item.getCode());
            resultList.add(resultMap);
        }
        return resultList;
    }
}
