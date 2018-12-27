package com.ztdx.eams.controller.archives;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.application.ClassificationService;
import com.ztdx.eams.domain.archives.application.DescriptionItemService;
import com.ztdx.eams.domain.archives.application.DictionaryClassificationService;
import com.ztdx.eams.domain.archives.application.event.DescriptionItemAddEvent;
import com.ztdx.eams.domain.archives.application.event.DescriptionItemDeleteEvent;
import com.ztdx.eams.domain.archives.application.task.EntryAsyncTask;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.system.application.OrganizationService;
import com.ztdx.eams.domain.system.application.UserDesItemConfService;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/5/15.
 */
@RestController
@RequestMapping(value = "/descriptionItem")
public class DescriptionItemController {

    private final ArchivesQuery archivesQuery;

    private final DescriptionItemService descriptionItemService;

    private final ClassificationService classificationService;

    private final DictionaryClassificationService dictionaryClassificationService;

    private final OrganizationService organizationService;

    private final EntryAsyncTask entryAsyncTask;

    private final ApplicationContext applicationContext;


    @Autowired
    public DescriptionItemController(ArchivesQuery archivesQuery, DescriptionItemService descriptionItemService, ClassificationService classificationService, DictionaryClassificationService dictionaryClassificationService, OrganizationService organizationService, EntryAsyncTask entryAsyncTask, ApplicationContext applicationContext) {
        this.archivesQuery = archivesQuery;
        this.descriptionItemService = descriptionItemService;
        this.classificationService = classificationService;
        this.dictionaryClassificationService = dictionaryClassificationService;
        this.organizationService = organizationService;
        this.entryAsyncTask = entryAsyncTask;
        this.applicationContext = applicationContext;
    }

    /**
     * @api {get} /descriptionItem/list 获取目录的著录项
     * @apiName list
     * @apiGroup descriptionItem
     * @apiParam {Number} catalogueId 所属目录ID(url参数)
     * @apiSuccess (Success 200) {Number} id 著录项ID.
     * @apiSuccess (Success 200) {String} metadataName 元数据名称.
     * @apiSuccess (Success 200) {String} displayName 显示名称.
     * @apiSuccess (Success 200) {Number} propertyType 属性类型标识.
     * @apiSuccess (Success 200) {String} defaultValue 默认值.
     * @apiSuccess (Success 200) {Number} dataType 数据类型(1 数值 2 字符串 3 日期 4 浮点).
     * @apiSuccess (Success 200) {Number} isIncrement 是否自增(0 否 1 是).
     * @apiSuccess (Success 200) {Number} isRead 是否只读(0 否 1 是).
     * @apiSuccess (Success 200) {Number} isNull 是否可空(0 否 1 是).
     * @apiSuccess (Success 200) {Number} isDictionary 是否使用字典(0 否 1 是).
     * @apiSuccess (Success 200) {Number} dictionaryType 字典类型(1 目录字典 2 档案分类 3 组织机构）.
     * @apiSuccess (Success 200) {Number} dictionaryNodeId 字典节点标识（字典类型为目录字典时为字典分类ID，字典类型为档案分类时为上级档案分类节点ID，字典类型为目录字典时为上级组织机构节点ID）.
     * @apiSuccess (Success 200) {Number} dictionaryValueType 字典获取值的方式(1 编码 2 名称 3 编码与名称 4 名称与编码）.
     * @apiSuccess (Success 200) {Number} dictionaryRootSelect 字典中，是否根节点可选(0 不可选 1 可选)(当字典类型为“档案分类”、“组织结构”时有效).
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 著录项ID,"displayName": "显示名称","propertyType": 属性类型标识,"defaultValue": "默认值","dataType": 数据类型,"isRead": 是否只读
     * ,"isNull": 是否可空,"isDictionary": 是否使用字典,"dictionaryType": 字典类型,"dictionaryNodeId": 字典节点标识,"dictionaryValueType": 字典获取值的方式,"dictionaryRootSelect": 字典中是否根节点可选,}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list(@RequestParam("catalogueId") int catalogueId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = archivesQuery.getDescriptionItemList(UInteger.valueOf(catalogueId));
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * @api {get} /descriptionItem/listForPlaceOnFile?archivesId={archivesId} 通过档案库ID获取目录ID及著录项
     * @apiName listForPlaceOnFile
     * @apiGroup descriptionItem
     * @apiParam {Number} archivesId 档案库ID(url参数)
     * @apiSuccess (Success 200) {Number} catalogueId 目录ID.
     * @apiSuccess (Success 200) {String} type 目录类型(可选项：一文一件 file，案卷 folder，卷内 folderFile，项目 subject).
     * @apiSuccess (Success 200) {Object[]} descriptionItem 著录项集合.
     * @apiSuccess (Success 200) {Number} descriptionItemId 著录项ID.
     * @apiSuccess (Success 200) {String} metadataName 元数据名称.
     * @apiSuccess (Success 200) {String} displayName 显示名称.
     * @apiSuccessExample {json} Success-Response:
     * {
     * "data": {
     * "item": [
     * {
     * "catalogueId": 3,
     * "type": "folder",
     * "descriptionItem": [
     * {
     * "descriptionItemId": 10,
     * "metadataName": "name",
     * "displayName": "姓名"
     * },
     * {
     * "descriptionItemId": 11,
     * "metadataName": "age",
     * "displayName": "年龄"
     * },
     * {
     * "descriptionItemId": 12,
     * "metadataName": "birthday",
     * "displayName": "生日"
     * },
     * {
     * "descriptionItemId": 13,
     * "metadataName": "amount",
     * "displayName": "资产"
     * },
     * {
     * "descriptionItemId": 14,
     * "metadataName": "fenlei",
     * "displayName": "分类"
     * },
     * {
     * "descriptionItemId": 15,
     * "metadataName": "zidian",
     * "displayName": "字典"
     * },
     * {
     * "descriptionItemId": 16,
     * "metadataName": "jigou",
     * "displayName": "机构"
     * },
     * {
     * "descriptionItemId": 78,
     * "metadataName": "danghao",
     * "displayName": "档号"
     * }
     * ]
     * },
     * {
     * "catalogueId": 5,
     * "type": "folderFile",
     * "descriptionItem": [
     * {
     * "descriptionItemId": 31,
     * "metadataName": "name",
     * "displayName": "姓名"
     * },
     * {
     * "descriptionItemId": 32,
     * "metadataName": "age",
     * "displayName": "年龄"
     * },
     * {
     * "descriptionItemId": 33,
     * "metadataName": "birthday",
     * "displayName": "生日"
     * },
     * {
     * "descriptionItemId": 34,
     * "metadataName": "amount",
     * "displayName": "资产"
     * },
     * {
     * "descriptionItemId": 35,
     * "metadataName": "fenlei",
     * "displayName": "分类"
     * },
     * {
     * "descriptionItemId": 36,
     * "metadataName": "zidian",
     * "displayName": "字典"
     * },
     * {
     * "descriptionItemId": 37,
     * "metadataName": "jigou",
     * "displayName": "机构"
     * },
     * {
     * "descriptionItemId": 77,
     * "metadataName": "danghao",
     * "displayName": "档号"
     * },
     * {
     * "descriptionItemId": 79,
     * "metadataName": "sn",
     * "displayName": "序号"
     * }
     * ]
     * }
     * ]
     * }
     * }
     */
    @RequestMapping(value = "/listForPlaceOnFile", method = RequestMethod.GET)
    public Map<String, Object> listForPlaceOnFile(@RequestParam("archivesId") int archivesId) {
        return archivesQuery.getDescriptionItemListForPlaceOnFile(UInteger.valueOf(archivesId));
    }

    /**
     * @api {post} /descriptionItem 新增著录项
     * @apiName save
     * @apiGroup descriptionItem
     * @apiParam {Number} catalogueId 目录ID
     * @apiParam {Number[]} metadataIds 元数据ID
     * @apiError (Error 400) message 1.目录不存在 2.元数据不在同一个元数据规范中.
     * @apiUse ErrorExample
     */
    @Transactional
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Map map) {
        int catalogueId = (int) map.get("catalogueId");
        List<Integer> metadataIds = (List<Integer>) map.get("metadataIds");
        List<DescriptionItem> descriptionItems = descriptionItemService.save(catalogueId, metadataIds);
        entryAsyncTask.putMapping(descriptionItems);

        applicationContext.publishEvent(new DescriptionItemAddEvent(this, descriptionItems));
    }

    /**
     * @api {delete} /descriptionItem/ 删除著录项
     * @apiName delete
     * @apiGroup descriptionItem
     * @apiParam {Number[]} ids 著录项ID（url占位符）
     * @apiParamExample {json} Request-Example:
     * [1,2,3]
     */
    @Transactional
    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public void delete(@RequestBody List<Integer> ids) {
        if(ids.size()==0){
            throw new InvalidArgumentException("选择要删除的著录项");
        }
        applicationContext.publishEvent(new DescriptionItemDeleteEvent(this, ids));
        descriptionItemService.delete(ids);
    }

    /**
     * @api {put} /descriptionItem 修改著录项信息
     * @apiName update
     * @apiGroup descriptionItem
     * @apiParam {Number} id 著录项ID.
     * @apiParam {String} displayName 显示名称.
     * @apiParam {Number} propertyType 字段属性.
     * @apiParam {Number} defaultValue 默认值.
     * @apiParam {Number} dataType 数据类型(1 数值 2 字符串 3 日期 4 浮点).
     * @apiParam {Number} fieldWidth 字典宽度.
     * @apiParam {Number} fieldPrecision 字段精度.
     * @apiParam {Number} isIncrement 是否自增(0 否 1 是).
     * @apiParam {Number} isRead 是否只读(0 否 1 是).
     * @apiParam {Number} isNull 是否可空(0 否 1 是).
     * @apiParam {Number} isDictionary 是否使用字典(0 否 1 是).
     * @apiParam {Number} dictionaryType 字典类型(1 目录字典 2 档案分类 3 组织机构）.
     * @apiParam {Number} dictionaryNodeId 字典节点标识（字典类型为目录字典时为字典分类ID，字典类型为档案分类时为上级档案分类节点ID，字典类型为目录字典时为上级组织机构节点ID）.
     * @apiParam {Number} dictionaryValueType 字典获取值的方式(1 编码 2 名称 3 编码与名称 4 名称与编码）.
     * @apiParam {Number} dictionaryRootSelect 字典中，是否根节点可选(0 不可选 1 可选)(当字典类型为“档案分类”、“组织结构”时有效).
     * @apiError (Error 400) message .
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody DescriptionItem descriptionItem) {
        if (descriptionItem.getIsDictionary() == 1) {
            if (descriptionItem.getDictionaryType() == 1 && null == dictionaryClassificationService.get(descriptionItem.getDictionaryNodeId())) {
                throw new InvalidArgumentException("字典分类不存在");
            }
            if (descriptionItem.getDictionaryType() == 2 && null == classificationService.get(descriptionItem.getDictionaryNodeId())) {
                throw new InvalidArgumentException("档案分类不存在");
            }
            if (descriptionItem.getDictionaryType() == 3 && null == organizationService.get(descriptionItem.getDictionaryNodeId())) {
                throw new InvalidArgumentException("组织机构不存在");
            }
        }
        descriptionItemService.update(descriptionItem);
    }

    /**
     * @api {get} /descriptionItem/{id} 获取著录项详情
     * @apiName get
     * @apiGroup descriptionItem
     * @apiParam {Number} id 著录项ID（url占位符）
     * @apiSuccess (Success 200) {Number} id 著录项ID.
     * @apiSuccess (Success 200) {String} metadataName 元数据名称.
     * @apiSuccess (Success 200) {String} displayName 显示名称.
     * @apiSuccess (Success 200) {Number} propertyType 字段属性.
     * @apiSuccess (Success 200) {Number} defaultValue 默认值.
     * @apiSuccess (Success 200) {Number} dataType 数据类型(1 数值 2 字符串 3 日期 4 浮点).
     * @apiSuccess (Success 200) {Number} fieldWidth 字典宽度.
     * @apiSuccess (Success 200) {Number} fieldPrecision 字段精度.
     * @apiSuccess (Success 200) {Number} isIncrement 是否自增(0 否 1 是).
     * @apiSuccess (Success 200) {Number} isRead 是否只读(0 否 1 是).
     * @apiSuccess (Success 200) {Number} isNull 是否可空(0 否 1 是).
     * @apiSuccess (Success 200) {Number} isDictionary 是否使用字典(0 否 1 是).
     * @apiSuccess (Success 200) {Number} dictionaryType 字典类型(1 目录字典 2 档案分类 3 组织机构）.
     * @apiSuccess (Success 200) {Number} dictionaryNodeId 字典节点标识（字典类型为目录字典时为字典分类ID，字典类型为档案分类时为上级档案分类节点ID，字典类型为目录字典时为上级组织机构节点ID）.
     * @apiSuccess (Success 200) {Number} dictionaryValueType 字典获取值的方式(1 编码 2 名称 3 编码与名称 4 名称与编码）.
     * @apiSuccess (Success 200) {Number} dictionaryRootSelect 字典中，是否根节点可选(0 不可选 1 可选)(当字典类型为“档案分类”、“组织结构”时有效).
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 档案分类ID,"code": "档案分类编码","name": "档案分类名称","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return archivesQuery.getDescriptionItem(UInteger.valueOf(id));
    }
}
