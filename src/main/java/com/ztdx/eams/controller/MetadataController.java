package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.MetadataService;
import com.ztdx.eams.domain.archives.model.Metadata;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by li on 2018/4/22.
 */
@RestController
@RequestMapping(value = "/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    private final ArchivesQuery archivesQuery;
    @Autowired
    public MetadataController(MetadataService metadataService, ArchivesQuery archivesQuery) {
        this.metadataService = metadataService;
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /metadata/list 获取元数据列表
     * @apiName list
     * @apiDescription 用于通过名称与元数据规范ID获取元数据列表
     * @apiGroup metadata
     * @apiParam {String} metadataStandardsId 元数据规范ID(url参数)
     * @apiParam {String} name 名称(未输入传""值)(url参数)
     * @apiSuccess (Success 200) {int} id ID.
     * @apiSuccess (Success 200) {String} displayName 字段显示名.
     * @apiSuccess (Success 200) {String} name 字段名称.
     * @apiSuccess (Success 200) {int} fieldProperties 字段属性.
     * @apiSuccess (Success 200) {int} dataType 数据类型.
     * @apiSuccess (Success 200) {String} fieldWidth 字段宽度.
     * @apiSuccess (Success 200) {String} fieldPrecision 字段精度.
     * @apiSuccess (Success 200) {int} parentId 父字段ID.
     * @apiSuccess (Success 200) {int} defaultValue 默认值.
     * @apiSuccess (Success 200) {String} definition 定义.
     * @apiSuccess (Success 200) {String} objective 目的.
     * @apiSuccess (Success 200) {int} constraint 约束性.
     * @apiSuccess (Success 200) {int} elementType 元素类型.
     * @apiSuccess (Success 200) {String} codingModification 编码修饰体系.
     * @apiSuccess (Success 200) {String} relatedElements 相关元素.
     * @apiSuccess (Success 200) {String} range 值域.
     * @apiSuccess (Success 200) {String} informationSources 信息来源.
     * @apiSuccess (Success 200) {String} orderNumber 排序号.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": ID,"displayName": "字段显示名","name": "字段名称",
     * "dataType": "数据类型","fieldWidth": "字段宽度","fieldPrecision": "字段精度",
     * "parentId": 父字段ID,"defaultValue": "默认值","fieldProperties": 字段属性,
     * "definition": 定义,"objective": 目的,"constraint": "约束性",
     * "elementType": 元素类型,"codingModification": 编码修饰体系,"relatedElements": "相关元素",
     * "range": 值域,"informationSources": 信息来源,"orderNumber": "排序号","remark": "备注"}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list(@RequestParam(value = "metadataStandardsId") int metadataStandardsId,@RequestParam(value = "name",defaultValue = "") String name) {
        return archivesQuery.getMetadataList(UInteger.valueOf(metadataStandardsId),name);
    }

    /**
     * @api {post} /metadata 新增元数据
     * @apiName save
     * @apiGroup metadata
     * @apiParam {String} displayName 字段显示名.
     * @apiParam {String} name 字段名称.
     * @apiParam {int} fieldProperties 字段属性.
     * @apiParam {int} dataType 数据类型.
     * @apiParam {String} fieldWidth 字段宽度.
     * @apiParam {String} fieldPrecision 字段精度.
     * @apiParam {int} parentId 父字段ID.
     * @apiParam {int} metadataStandardsId 元数据规范ID.
     * @apiParam {int} defaultValue 默认值.
     * @apiParam {String} definition 定义.
     * @apiParam {String} objective 目的.
     * @apiParam {int} constraint 约束性.
     * @apiParam {int} elementType 元素类型.
     * @apiParam {String} codingModification 编码修饰体系.
     * @apiParam {String} relatedElements 相关元素.
     * @apiParam {String} range 值域.
     * @apiParam {String} informationSources 信息来源.
     * @apiParam {String} remark 备注.
     * @apiError (Error 400) message 字段名称已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Metadata metadata) {
        metadataService.save(metadata);
    }

    /**
     * @api {delete} /metadata/{id} 删除元数据
     * @apiName delete
     * @apiGroup metadata
     * @apiParam {int} id ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        metadataService.delete(id);
    }

    /**
     * @api {put} /metadata 修改元数据
     * @apiName update
     * @apiGroup metadata
     * @apiParam {int} id ID
     * @apiParam {String} displayName 字段显示名.
     * @apiParam {String} name 字段名称.
     * @apiParam {int} fieldProperties 字段属性.
     * @apiParam {int} dataType 数据类型.
     * @apiParam {String} fieldWidth 字段宽度.
     * @apiParam {String} fieldPrecision 字段精度.
     * @apiParam {int} parentId 父字段ID.
     * @apiParam {int} defaultValue 默认值.
     * @apiParam {String} definition 定义.
     * @apiParam {String} objective 目的.
     * @apiParam {int} constraint 约束性.
     * @apiParam {int} elementType 元素类型.
     * @apiParam {String} codingModification 编码修饰体系.
     * @apiParam {String} relatedElements 相关元素.
     * @apiParam {String} range 值域.
     * @apiParam {String} informationSources 信息来源.
     * @apiParam {String} remark 备注.
     * @apiError (Error 400) message 字段名称已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Metadata metadata) {
        metadataService.update(metadata);
    }

    /**
     * @api {get} /metadata/{id} 获取元数据详情
     * @apiName get
     * @apiGroup metadata
     * @apiParam int id 词典ID（url占位符）
     * @apiSuccess (Success 200) {int} id ID.
     * @apiSuccess (Success 200) {String} displayName 字段显示名.
     * @apiSuccess (Success 200) {String} name 字段名称.
     * @apiSuccess (Success 200) {int} fieldProperties 字段属性.
     * @apiSuccess (Success 200) {int} dataType 数据类型.
     * @apiSuccess (Success 200) {String} fieldWidth 字段宽度.
     * @apiSuccess (Success 200) {String} fieldPrecision 字段精度.
     * @apiSuccess (Success 200) {int} parentId 父字段ID.
     * @apiSuccess (Success 200) {int} metadataStandardsId 元数据规范ID.
     * @apiSuccess (Success 200) {int} defaultValue 默认值.
     * @apiSuccess (Success 200) {String} definition 定义.
     * @apiSuccess (Success 200) {String} objective 目的.
     * @apiSuccess (Success 200) {int} constraint 约束性.
     * @apiSuccess (Success 200) {int} elementType 元素类型.
     * @apiSuccess (Success 200) {String} codingModification 编码修饰体系.
     * @apiSuccess (Success 200) {String} relatedElements 相关元素.
     * @apiSuccess (Success 200) {String} range 值域.
     * @apiSuccess (Success 200) {String} informationSources 信息来源.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": ID,"displayName": "字段显示名","name": "字段名称","fieldProperties": 字段属性,
     * "dataType": "数据类型","fieldWidth": "字段宽度","fieldPrecision": "字段精度",
     * "parentId": 父字段ID,"metadataStandardsId": 元数据规范ID,"defaultValue": "默认值",
     * "definition": 定义,"objective": 目的,"constraint": "约束性",
     * "elementType": 元素类型,"codingModification": 编码修饰体系,"relatedElements": "相关元素",
     * "range": 值域,"informationSources": 信息来源,"orderNumber": "排序号","remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return archivesQuery.getMetadata(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /metadata/{upId},{downId}/priority 修改元数据排序优先级
     * @apiName priority
     * @apiGroup metadata
     * @apiParam {int} upId 上移词典ID（url占位符）
     * @apiParam {int} downId 下移词典ID（url占位符）
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        metadataService.priority(upId, downId);
    }
}
