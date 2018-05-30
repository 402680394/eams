package com.ztdx.eams.controller;

import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.archives.application.ArchivalCodeRulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *档号生成与清除
 */
@RestController
@RequestMapping(value = "/archivalCode")
public class ArchivalCodeRulerController {

    private final ArchivalCodeRulerService archivalcodeRulerService;

    @Autowired
    public ArchivalCodeRulerController(ArchivalCodeRulerService archivalcodeRulerService) {
        this.archivalcodeRulerService = archivalcodeRulerService;
    }

    /**
     * @api {put} /archivalCode/generating 生成档号
     * @apiName generating
     * @apiGroup archivalCode
     * @apiParam {String[] } entryIds 条目id集合.
     * @apiParam {Number} Catalogue:id 目录ID.
     * @apiSuccess (Success 200) {String[]} content 列表内容.
     * @apiParamExample {json} Success-Response:
     *  [
     *    "档案A 档号生成失败，错误原因：档号已经存在。",
     *    "档案A 档号生成失败，错误原因：XXX不能为空。"
     *  ]
     */
    @RequestMapping(value = "/generating", method = RequestMethod.PUT)
    public List<String> generating(@JsonParam List<String> entryIds, @JsonParam int catalogueId) {
        return archivalcodeRulerService.generating(entryIds, catalogueId);

    }


    /**
     * @api {put} /archivalCode/clear 清除档号
     * @apiName clear
     * @apiGroup archivalCode
     * @apiParam {String[] } entryIds 条目id集合.
     * @apiParam {Number} catalogueId 档案目录id.
     * @apiParamExample {json} Success-Response:
     */
    @RequestMapping(value = "/clear", method = RequestMethod.PUT)
    public void clear(@JsonParam List<String> entryIds,@JsonParam int catalogueId) {
        archivalcodeRulerService.clear(entryIds, catalogueId);
    }
}