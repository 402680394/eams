package com.ztdx.eams.controller.archives;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.archives.application.ArchivalCodeRulerService;
import com.ztdx.eams.domain.archives.application.DescriptionItemService;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.archivalCodeRuler.ArchivalCodeRuler;
import com.ztdx.eams.domain.archives.model.archivalCodeRuler.RulerType;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 档号生成与清除
 */
@RestController
@RequestMapping(value = "/archivalCode")
public class ArchivalCodeRulerController {

    private final ArchivalCodeRulerService archivalCodeRulerService;

    private final ArchivesQuery archivesQuery;

    private final DescriptionItemService descriptionItemService;

    @Autowired
    public ArchivalCodeRulerController(ArchivalCodeRulerService archivalCodeRulerService, ArchivesQuery archivesQuery, DescriptionItemService descriptionItemService) {
        this.archivalCodeRulerService = archivalCodeRulerService;
        this.archivesQuery = archivesQuery;
        this.descriptionItemService = descriptionItemService;
    }

    /**
     * @api {put} /archivalCode/generating/fileAndFolder 生成一文一件及案卷档号
     * @apiName generating/fileAndFolder
     * @apiGroup archivalCode
     * @apiParam {String[] } entryIds 条目id集合.
     * @apiParam {Number} catalogueId目录ID.
     * @apiSuccess (Success 200) {String[]} content 列表内容.
     * @apiErrorExample {json} Error-Response:
     * [
     * "档案A 档号生成失败，错误原因：档号已经存在。",
     * "档案A 档号生成失败，错误原因：XXX不能为空。"
     * ]
     */
    @RequestMapping(value = "/generating/fileAndFolder", method = RequestMethod.PUT)
    public List<Map<String, String>> fileAndFolder(@JsonParam List<String> entryIds, @JsonParam int catalogueId) {
        return archivalCodeRulerService.generatingFileAndFolder(entryIds, catalogueId);
    }

    /**
     * @api {put} /archivalCode/generating/folderFile 生成卷内档号
     * @apiName generating/folderFile
     * @apiGroup archivalCode
     * @apiParam {String} folderId 案卷id.
     * @apiParam {Number} catalogueId目录ID.
     * @apiSuccess (Success 200) {String[]} content 列表内容.
     * @apiErrorExample {json} Error-Response:
     * [
     * "档案A 档号生成失败，错误原因：档号已经存在。",
     * "档案A 档号生成失败，错误原因：XXX不能为空。"
     * ]
     */
    @RequestMapping(value = "/generating/folderFile", method = RequestMethod.PUT)
    public List<Map<String, String>> folderFile(@JsonParam String folderId, @JsonParam int catalogueId) {
        return archivalCodeRulerService.generatingFolderFile(folderId, catalogueId);
    }


    /**
     * @api {put} /archivalCode/clear 清除档号
     * @apiName clear
     * @apiGroup archivalCode
     * @apiParam {String[] } entryIds 条目id集合.
     * @apiParam {Number} catalogueId 档案目录id.
     */
    @RequestMapping(value = "/clear", method = RequestMethod.PUT)
    public void clear(@JsonParam List<String> entryIds, @JsonParam int catalogueId) {
        archivalCodeRulerService.clear(entryIds, catalogueId);
    }

    /**
     * @api {get} /archivalCode/list 通过目录id查询档号规则列表
     * @apiName list
     * @apiGroup archivalCode
     * @apiParam {Number} catalogueId 目录ID(url参数)
     * @apiSuccess (Success 200) {Number} type 规则类型(1-著录项值 2-著录项所对应的参照编码 3-档案库所属单位全宗号 4-固定值 5-流水号).
     * @apiSuccess (Success 200) {String} value 值.
     * @apiSuccess (Success 200) {Number} interceptionLength 截取长度.
     * @apiSuccess (Success 200) Number} flowNumberLength 流水号长度.
     * @apiSuccess (Success 200) {Number} remark 备注.
     * @apiSuccess (Success 200) {Number} orderNumber 排序号.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"type": 1,"value": "值","interceptionLength": 4,"flowNumberLength": 4,"remark": "备注","orderNumber": 1}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list(@RequestParam("catalogueId") int catalogueId) {
        List<Map<String, Object>> rules = archivesQuery.getArchivalCodeRules(UInteger.valueOf(catalogueId));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("items", rules);
        return resultMap;
    }

    /**
     * @api {post} /archivalCode 新增档号规则
     * @apiName save
     * @apiGroup archivalCode
     * @apiParam {Number} type 类型 (1-著录项值 2-著录项所对应的参照编码 3-档案库所属单位全宗号 4-固定值 5-流水号)
     * @apiParam {String{10}} value 值(类型为固定值时传递)
     * @apiParam {Number} descriptionItemId 著录项id(类型为著录项值时传递)
     * @apiParam {Number} interceptionLength 截取长度(类型为著录项值时传递)
     * @apiParam {Number} flowNumberLength 流水号长度(类型为流水号时传递)
     * @apiParam {Number} catalogueId 目录ID
     * @apiParam {String{50}} remark 备注（未输入传""值）
     * @apiError (Error 400) message .
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody ArchivalCodeRuler archivalCodeRuler) {
        this.validate(archivalCodeRuler);
        archivalCodeRulerService.save(archivalCodeRuler);
    }

    /**
     * @api {delete} /archivalCode/{id} 删除档号规则
     * @apiName delete
     * @apiGroup archivalCode
     * @apiParam {Number} id 档号规则ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        archivalCodeRulerService.delete(id);
    }

    /**
     * @api {put} /archivalCode 修改档号规则
     * @apiName update
     * @apiGroup archivalCode
     * @apiParam {Number} id ID
     * @apiParam {Number} type 类型 (1-著入项值 2-著录项所对应的参照编码 3-档案库所属单位全宗号 4-固定值 5-流水号)
     * @apiParam {String{10}} value 值(类型为固定值时传递)
     * @apiParam {Number} descriptionItemId 著录项id(类型为著录项值时传递)
     * @apiParam {Number} interceptionLength 截取长度(类型为著录项值时传递)
     * @apiParam {Number} flowNumberLength 流水号长度(类型为流水号时传递)
     * @apiParam {Number} catalogueId 目录ID
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message .
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody ArchivalCodeRuler archivalCodeRuler) {
        this.validate(archivalCodeRuler);
        archivalCodeRulerService.update(archivalCodeRuler);
    }

    /**
     * @api {get} /archivalCode/{id} 获取档号规则详情
     * @apiName get
     * @apiGroup archivalCode
     * @apiParam {Number} id 档号规则ID（url占位符）
     * @apiSuccess (Success 200) {Number} id 档号规则ID.
     * @apiSuccess (Success 200) {Number} type 类型 (1-著入项值 2-著录项所对应的参照编码 3-档案库所属单位全宗号 4-固定值)
     * @apiSuccess (Success 200) {String} value 值.
     * @apiSuccess (Success 200) {Number} descriptionItemId 著录项id.
     * @apiSuccess (Success 200) {Number} interceptionLength 截取长度.
     * @apiSuccess (Success 200) {Number} catalogueId 目录ID.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 2,"type": 1,"value": "值","descriptionItemId": 3,"interceptionLength": 6,"catalogueId": 2,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return archivesQuery.getArchivalCodeRuler(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /archivalCode/{upId},{downId}/priority 修改档号规则排序号
     * @apiName priority
     * @apiGroup archivalCode
     * @apiParam {Number} upId 上移档号规则ID（url占位符）
     * @apiParam {Number} downId 下移档号规则ID（url占位符）
     * @apiError (Error 400) message 流水号只能位于最后.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PUT)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        archivalCodeRulerService.priority(upId, downId);
    }

    private void validate(ArchivalCodeRuler archivalCodeRuler) {
        if (archivalCodeRuler.getType().equals(RulerType.EntryValue) || archivalCodeRuler.getType().equals(RulerType.ReferenceCode)) {

            List<DescriptionItem> descriptionItems = descriptionItemService.findByCatalogueId(archivalCodeRuler.getCatalogueId());

            boolean b = false;
            for (DescriptionItem descriptionItem : descriptionItems) {
                if (archivalCodeRuler.getDescriptionItemId().equals(descriptionItem.getId())) {
                    b = true;
                    archivalCodeRuler.setValue(descriptionItem.getDisplayName());
                }
            }
            if (!b) {
                throw new InvalidArgumentException("著录项校验错误");
            }
        } else if (archivalCodeRuler.getType().equals(RulerType.FondsCode)) {
            archivalCodeRuler.setValue("全宗号");
        }
        if (null == archivalCodeRuler.getRemark()) {
            archivalCodeRuler.setRemark("");
        }
        if (null == archivalCodeRuler.getValue()) {
            archivalCodeRuler.setValue("");
        }
        if (archivalCodeRuler.getFlowNumberLength() <= 0) {
            archivalCodeRuler.setFlowNumberLength((byte) 0);
        }
    }
}