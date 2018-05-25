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

@RestController
@RequestMapping(value = "/archivalCode")
public class ArchivalCodeRulerController {

    private final ArchivalCodeRulerService archivalcodeRulerService;

    @Autowired
    public ArchivalCodeRulerController(ArchivalCodeRulerService archivalcodeRulerService) {
        this.archivalcodeRulerService = archivalcodeRulerService;
    }

    /**
     * @api {post} /archivalCode/generating 生成档号
     * @apiName generating
     * @apiGroup archivalCode
     * @apiSuccess (Success 200) {String[]} id  ID.
     * @apiSuccess (Success 200) {Number} type 类型(1 著入项值；2 著录项所对应的参照编码；3 档案所属单位全宗号；4 固定值）
     * @apiSuccess (Success 200) {String} metadataName 元数据.
     * @apiSuccess (Success 200) {String} value 值.
     * @apiSuccess (Success 200) {Number} interceptionLength 截取长度.
     * @apiSuccess (Success 200) {String} remark 备注
     * @apiSuccess (Success 200) {Number} orderNumber 排序号.
     * @apiSuccess (Success 200) {Number} Catalogue:id 目录ID.
     * @apiParamExample {json} Request-Example:
     *  ["档案A 档号生成失败，错误原因：档号已经存在。","",""]
     * {
     *      "id":ID,"
     *      "type":类型,
     *      "metadataName":"元数据",
     *      "value":"值",
     *      "interceptionLength":截取长度,
     *      "remark":"备注",
     *      "orderNumber":排序号,
     *      "Catalogue","id":目录ID
     * }
     */
    @RequestMapping(value = "/generating", method = RequestMethod.POST)
    public List<String> generating(@JsonParam List<UUID> entryIds, @JsonParam int catalogueId) {
        return archivalcodeRulerService.generating(entryIds, catalogueId);

    }

    /**
     * @api {get} /archivalCode/clear 清除档号
     * @apiName clear
     * @apiGroup archivalCode
     * @apiSuccess (Success 200) {Number} Entry:id.
     * @apiSuccess (Success 200) {Number} Catalogue:id 目录ID.
     * @apiParamExample {json} Request-Example:
     *
     * {
     *      "id":ID,"
     *      "type":类型,
     *      "metadataName":"元数据",
     *      "value":"值",
     *      "interceptionLength":截取长度,
     *      "remark":"备注",
     *      "orderNumber":排序号,
     *      "Catalogue","id":目录ID
     * }
     */
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public void clear(@JsonParam List<UUID> entryIds,@JsonParam int catalogueId) {
        archivalcodeRulerService.clear(entryIds, catalogueId);
    }
}