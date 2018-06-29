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
 * Created by li on 2018/5/15.
 */
@RestController
@RequestMapping(value = "/descriptionItem")
public class DescriptionItemController {

    private final ArchivesQuery archivesQuery;

    @Autowired
    public DescriptionItemController(ArchivesQuery archivesQuery) {
        this.archivesQuery = archivesQuery;
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
        return archivesQuery.getDescriptionItemList(UInteger.valueOf(catalogueId));
    }

    /**
     * @api {get} /descriptionItem/listForPlaceOnFile?archivesId={archivesId} 通过档案库ID获取目录ID及著录项
     * @apiName listForPlaceOnFile
     * @apiGroup descriptionItem
     * @apiParam {Number} archivesId 档案库ID(url参数)
     * @apiSuccess (Success 200) {Number} catalogueId 目录ID.
     * @apiSuccess (Success 200) {String} type 目录类型(可选项：一文一件，案卷，卷内，项目).
     * @apiSuccess (Success 200) {Object[]} descriptionItem 著录项集合.
     * @apiSuccess (Success 200) {Number} descriptionItemId 著录项ID.
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
     * "displayName": "姓名"
     * },
     * {
     * "descriptionItemId": 11,
     * "displayName": "年龄"
     * },
     * {
     * "descriptionItemId": 12,
     * "displayName": "生日"
     * },
     * {
     * "descriptionItemId": 13,
     * "displayName": "资产"
     * },
     * {
     * "descriptionItemId": 14,
     * "displayName": "分类"
     * },
     * {
     * "descriptionItemId": 15,
     * "displayName": "字典"
     * },
     * {
     * "descriptionItemId": 16,
     * "displayName": "机构"
     * },
     * {
     * "descriptionItemId": 77,
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
     * "displayName": "姓名"
     * },
     * {
     * "descriptionItemId": 32,
     * "displayName": "年龄"
     * },
     * {
     * "descriptionItemId": 33,
     * "displayName": "生日"
     * },
     * {
     * "descriptionItemId": 34,
     * "displayName": "资产"
     * },
     * {
     * "descriptionItemId": 35,
     * "displayName": "分类"
     * },
     * {
     * "descriptionItemId": 36,
     * "displayName": "字典"
     * },
     * {
     * "descriptionItemId": 37,
     * "displayName": "机构"
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
}
