package com.ztdx.eams.controller;

import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by li on 2018/6/26.
 */
@RestController
@RequestMapping(value = "catalogue")
public class CatalogueController {

    private final ArchivesQuery archivesQuery;

    @Autowired
    public CatalogueController(ArchivesQuery archivesQuery) {
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /catalogue/byArchives?archivesId={archivesId} 通过档案库ID获取目录ID及类型，根据档案库类型返回不同结果
     * @apiName byArchives
     * @apiGroup catalogue
     * @apiParam {Number} archivesId 档案库ID
     * @apiSuccess (Success 200) {Number} file 一文一件目录ID(档案库类型为一文一件时返回该值).
     * @apiSuccess (Success 200) {Number} folder 传统立卷案卷目录ID(档案库类型为传统立卷时返回该值).
     * @apiSuccess (Success 200) {Number} folderFile 传统立卷卷内目录ID(档案库类型为传统立卷时返回该值).
     * @apiSuccess (Success 200) {Number} subject 项目目录ID(档案库类型为项目时返回该值).
     * @apiSuccessExample {json} Success-Response:
     * {"data": {folder:2,folderFile:3}}.
     */
    @RequestMapping(value = "/byArchives", method = RequestMethod.GET)
    public Map<String, Object> getCatalogueByArchivesId(@RequestParam("archivesId") int archivesId) {
        return archivesQuery.getCatalogueByArchivesId(UInteger.valueOf(archivesId));
    }
}
