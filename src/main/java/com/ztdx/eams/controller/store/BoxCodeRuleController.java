package com.ztdx.eams.controller.store;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.application.CatalogueService;
import com.ztdx.eams.domain.archives.application.DescriptionItemService;
import com.ztdx.eams.domain.archives.application.EntryService;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.store.application.BoxCodeRuleService;
import com.ztdx.eams.domain.store.model.BoxCodeRule;
import com.ztdx.eams.query.ArchivesQuery;
import com.ztdx.eams.query.StoreQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/7/11.
 */
@RestController
@RequestMapping(value = "boxCodeRule")
public class BoxCodeRuleController {

    private final StoreQuery storeQuery;

    private final ArchivesQuery archivesQuery;

    private final EntryService entryService;

    private final CatalogueService catalogueService;

    private final BoxCodeRuleService boxCodeRuleService;

    private final DescriptionItemService descriptionItemService;

    @Autowired
    public BoxCodeRuleController(StoreQuery storeQuery, ArchivesQuery archivesQuery, EntryService entryService, CatalogueService catalogueService, BoxCodeRuleService boxCodeRuleService, DescriptionItemService descriptionItemService) {
        this.storeQuery = storeQuery;
        this.archivesQuery = archivesQuery;
        this.entryService = entryService;
        this.catalogueService = catalogueService;
        this.boxCodeRuleService = boxCodeRuleService;
        this.descriptionItemService = descriptionItemService;
    }

    /**
     * @api {get} /boxCodeRule/ruleApply 通过档案库id查询盒号规则应用信息
     * @apiName list
     * @apiGroup boxCodeRule
     * @apiParam {Number} archivesId 所属库ID(url参数)
     * @apiParam {String} entryId 条目ID(url参数)(非必须)
     * @apiSuccess (Success 200) {Number} type 规则类型（1-著录项值 2-全宗号 3-固定值 4-流水号）.
     * @apiSuccess (Success 200) {String} name 名称.
     * @apiSuccess (Success 200) {String} value 值.
     * @apiSuccess (Success 200) {Number} isDictionary 是否使用字典(0 否 1 是).
     * @apiSuccess (Success 200) {Number} dictionaryType 字典类型(1 目录字典 2 档案分类 3 组织机构）.
     * @apiSuccess (Success 200) {Number} dictionaryNodeId 字典节点标识（字典类型为目录字典时为字典分类ID，字典类型为档案分类时为上级档案分类节点ID，字典类型为目录字典时为上级组织机构节点ID）.
     * @apiSuccess (Success 200) {Number} dictionaryValueType 字典获取值的方式(1 编码 2 名称 3 编码与名称 4 名称与编码）.
     * @apiSuccess (Success 200) {Number} dictionaryRootSelect 字典节点标识字典中，是否根节点可选(当字典类型为“档案分类”、“组织结构”时有效)(可选值：0-否 1-是).
     * @apiSuccess (Success 200) {Number} interceptionLength 截取长度(0表示截取全部).
     * @apiSuccess (Success 200) {Number} flowNumberLength 流水号长度.
     * @apiSuccess (Success 200) {Number} orderNumber 排序号.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"type": 1,"name": 名称,"value": "值","isDictionary": 1,"dictionaryType": 1,"dictionaryNodeId": 2,"dictionaryValueType": 2,"dictionaryRootSelect": 1,"interceptionLength": 4,"flowNumberLength": 1,"orderNumber": 1}]}}.
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_box_write')")
    @RequestMapping(value = "/ruleApply", method = RequestMethod.GET)
    public Map<String, Object> get(@RequestParam("archivesId") int archivesId, @RequestParam(name = "entryId", required = false, defaultValue = "") String entryId) {

        UInteger uarchivesId = UInteger.valueOf(archivesId);

        int catalogueId = archivesQuery.getCatalogueIdByArchivesIdAndType(uarchivesId).intValue();

        List<DescriptionItem> descriptionItems = descriptionItemService.findByCatalogueId(catalogueId);

        Entry entry = entryService.get(catalogueId, entryId);

        String fondsCode = archivesQuery.getFondsCodeByArchiveId(uarchivesId);

        List<Map<String, Object>> rules = storeQuery.getBoxCodeRulesByArchivesId(uarchivesId);

        for (Map rule : rules) {
            byte type = (byte) rule.get("type");
            //1-著录项值
            if (type == 1) {
                UInteger descriptionItemId = (UInteger) rule.get("descriptionItemId");
                for (DescriptionItem descriptionItem : descriptionItems) {
                    if (descriptionItemId.equals(descriptionItem.getId())) {
                        rule.put("isDictionary", descriptionItem.getIsDictionary());
                        rule.put("dictionaryType", descriptionItem.getDictionaryType());
                        rule.put("dictionaryNodeId", descriptionItem.getDictionaryNodeId());
                        rule.put("dictionaryValueType", descriptionItem.getDictionaryValueType());
                        rule.put("dictionaryRootSelect", descriptionItem.getDictionaryRootSelect());
                        rule.put("name", descriptionItem.getDisplayName());
                        if (null != entry) {
                            String metadataName = descriptionItem.getMetadataName();
                            rule.put("value", entry.getItems().get(metadataName));
                        } else {
                            rule.put("value", "");
                        }
                    }
                }
                //3-档案库所属全宗号
            } else if (type == 2) {
                rule.put("name", "全宗号");
                rule.put("value", fondsCode);
            } else if (type == 3) {
                rule.put("name", "固定值");
            } else if (type == 4) {
                rule.put("name", "流水号");
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("items", rules);
        return resultMap;
    }

    /**
     * @api {get} /boxCodeRule/list 通过目录id查询盒号规则列表
     * @apiName list
     * @apiGroup boxCodeRule
     * @apiParam {Number} archivesId 所属档案库ID(url参数)
     * @apiSuccess (Success 200) {Number} type 规则类型（1-著录项值 2-全宗号 3-固定值 4-流水号）.
     * @apiSuccess (Success 200) {String} value 值.
     * @apiSuccess (Success 200) {Number} interceptionLength 截取长度.
     * @apiSuccess (Success 200) {Number} flowNumberLength 流水号长度.
     * @apiSuccess (Success 200) {Number} remark 备注.
     * @apiSuccess (Success 200) {Number} orderNumber 排序号.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"type": 1,"value": "值","interceptionLength": 4,"flowNumberLength": 4,"remark": "备注","orderNumber": 1}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list(@RequestParam("archivesId") int archivesId) {
        List<Map<String, Object>> rules = storeQuery.getBoxCodeRulesByArchivesId(UInteger.valueOf(archivesId));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("items", rules);
        return resultMap;
    }

    /**
     * @api {post} /boxCodeRule 新增盒号规则
     * @apiName save
     * @apiGroup boxCodeRule
     * @apiParam {Number} type 类型 （1-著录项值 2-全宗号 3-固定值 4-流水号）
     * @apiParam {String{10}} value 值(类型为固定值时传递)
     * @apiParam {Number} descriptionItemId 著录项id(类型为著录项值时传递)
     * @apiParam {Number} interceptionLength 截取长度(类型为著录项值时传递)
     * @apiParam {Number} flowNumberLength 流水号长度(类型为流水号时传递)
     * @apiParam {Number} archivesId 档案库ID
     * @apiParam {String{50}} remark 备注（未输入传""值）
     * @apiError (Error 400) message 著录项校验错误.
     * @apiError (Error 500) message 只能有一个流水号.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody BoxCodeRule boxCodeRule) {
        this.validate(boxCodeRule);
        boxCodeRuleService.save(boxCodeRule);
    }

    /**
     * @api {delete} /boxCodeRule/{id} 删除盒号规则
     * @apiName delete
     * @apiGroup boxCodeRule
     * @apiParam {Number} id 盒号规则ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        boxCodeRuleService.delete(id);
    }

    /**
     * @api {put} /boxCodeRule 修改盒号规则
     * @apiName update
     * @apiGroup boxCodeRule
     * @apiParam {Number} id ID
     * @apiParam {Number} type 类型 （1-著录项值 2-全宗号 3-固定值 4-流水号）
     * @apiParam {String{10}} value 值(类型为固定值时传递)
     * @apiParam {Number} descriptionItemId 著录项id(类型为著录项值时传递)
     * @apiParam {Number} interceptionLength 截取长度(类型为著录项值时传递)
     * @apiParam {Number} flowNumberLength 流水号长度(类型为流水号时传递)
     * @apiParam {Number} archivesId 档案库ID
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 著录项校验错误.
     * @apiError (Error 500) message 只能有一个流水号.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody BoxCodeRule boxCodeRule) {
        this.validate(boxCodeRule);
        boxCodeRuleService.update(boxCodeRule);
    }

    /**
     * @api {get} /boxCodeRule/{id} 获取盒号规则详情
     * @apiName get
     * @apiGroup boxCodeRule
     * @apiParam {Number} id 档案库ID（url占位符）
     * @apiSuccess (Success 200) {Number} id 档案库ID.
     * @apiSuccess (Success 200) {Number} type 类型 （1-著录项值 2-全宗号 3-固定值 4-流水号）.
     * @apiSuccess (Success 200) {String} value 值.
     * @apiSuccess (Success 200) {Number} descriptionItemId 著录项id.
     * @apiSuccess (Success 200) {Number} interceptionLength 截取长度.
     * @apiSuccess (Success 200) {Number} flowNumberLength 流水号长度.
     * @apiSuccess (Success 200) {Number} archivesId 所属档案库ID.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 2,"type": 1,"value": "值","descriptionItemId": 3,"interceptionLength": 6,"flowNumberLength": 2,"archivesId": 2,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return storeQuery.getBoxCodeRule(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /boxCodeRule/{upId},{downId}/priority 修改盒号规则排序号
     * @apiName priority
     * @apiGroup boxCodeRule
     * @apiParam {Number} upId 上移盒号规则ID（url占位符）
     * @apiParam {Number} downId 下移盒号规则ID（url占位符）
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PUT)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        boxCodeRuleService.priority(upId, downId);
    }

    private void validate(BoxCodeRule boxCodeRule) {
        if (boxCodeRule.getType() == 1) {
            int catalogueId = archivesQuery.getCatalogueIdByArchivesIdAndType(UInteger.valueOf(boxCodeRule.getArchivesId())).intValue();

            List<DescriptionItem> descriptionItems = descriptionItemService.findByCatalogueId(catalogueId);

            boolean b = false;
            for (DescriptionItem descriptionItem : descriptionItems) {
                if (boxCodeRule.getDescriptionItemId().equals(descriptionItem.getId())) {
                    b = true;
                    boxCodeRule.setValue(descriptionItem.getDisplayName());
                }
            }
            if (!b) {
                throw new InvalidArgumentException("著录项校验错误");
            }
        } else if (boxCodeRule.getType() == 2) {
            boxCodeRule.setValue("全宗号");
        }
        if (null == boxCodeRule.getRemark()) {
            boxCodeRule.setRemark("");
        }
        if (null == boxCodeRule.getValue()) {
            boxCodeRule.setValue("");
        }
        if (boxCodeRule.getFlowNumberLength() <= 0) {
            boxCodeRule.setFlowNumberLength((byte) 0);
        }
    }

}
