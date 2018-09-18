package com.ztdx.eams.controller.archives;

import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by li on 2018/5/10.
 */
@RestController
@RequestMapping(value = "/contentType")
public class ContentTypeController {

    private final ArchivesQuery archivesQuery;

    @Autowired
    public ContentTypeController(ArchivesQuery archivesQuery) {
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /contentType/list 获取档案库类型列表
     * @apiName list
     * @apiGroup contentType
     * @apiSuccess (Success 200) {Number} id ID.
     * @apiSuccess (Success 200) {String} name 名称.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": ID,"name": "名称"}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("items", archivesQuery.getContentTypeList());
        return resultMap;
    }
}
