package com.ztdx.eams.controller;

import com.ztdx.eams.query.ArchivesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by li on 2018/5/10.
 */
@RestController
@RequestMapping(value = "/fileType")
public class FileTypeController {

    private final ArchivesQuery archivesQuery;

    @Autowired
    public FileTypeController(ArchivesQuery archivesQuery) {
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /fileType/list 获取文件类型列表
     * @apiName list
     * @apiGroup fileType
     * @apiSuccess (Success 200) {int} id ID.
     * @apiSuccess (Success 200) {String} name 名称.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": ID,"name": "名称"}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list() {
        return archivesQuery.getFileTypeList();
    }
}
