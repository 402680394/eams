package com.ztdx.eams.controller.archives;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.domain.archives.application.DescriptionItemService;
import com.ztdx.eams.domain.archives.application.MetadataService;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.Metadata;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/4/22.
 */
@RestController
@RequestMapping(value = "/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    private final ArchivesQuery archivesQuery;

    private final DescriptionItemService descriptionItemService;
    @Autowired
    public MetadataController(MetadataService metadataService, ArchivesQuery archivesQuery, DescriptionItemService descriptionItemService) {
        this.metadataService = metadataService;
        this.archivesQuery = archivesQuery;
        this.descriptionItemService = descriptionItemService;
    }

    /**
     * @api {get} /metadata/list 获取元数据列表
     * @apiName list
     * @apiDescription 用于通过名称与元数据规范ID获取元数据列表
     * @apiGroup metadata
     * @apiParam {Number} metadataStandardsId 元数据规范ID(url参数)
     * @apiParam {String{30}} name 名称(非必传)(url参数)
     * @apiSuccess (Success 200) {Number} id ID.
     * @apiSuccess (Success 200) {String} displayName 字段显示名.
     * @apiSuccess (Success 200) {String} name 字段名称.
     * @apiSuccess (Success 200) {Number} fieldProperties 字段属性.
     * @apiSuccess (Success 200) {Number} dataType 数据类型.
     * @apiSuccess (Success 200) {String} fieldWidth 字段宽度.
     * @apiSuccess (Success 200) {String} fieldPrecision 字段精度.
     * @apiSuccess (Success 200) {Number} parentId 父字段ID.
     * @apiSuccess (Success 200) {Number} defaultValue 默认值.
     * @apiSuccess (Success 200) {String} definition 定义.
     * @apiSuccess (Success 200) {String} objective 目的.
     * @apiSuccess (Success 200) {Number} constraint 约束性.
     * @apiSuccess (Success 200) {Number} elementType 元素类型.
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
    public Map<String, Object> list(@RequestParam(value = "metadataStandardsId") int metadataStandardsId
            ,@RequestParam(value = "name",defaultValue = "") String name
            , @RequestParam(name = "pageSize", required = false, defaultValue = "15") int pageSize
            , @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum) {
        return archivesQuery.getMetadataList(UInteger.valueOf(metadataStandardsId),name, pageNum, pageSize);
    }

    /**
     * @api {post} /metadata 新增元数据
     * @apiName save
     * @apiGroup metadata
     * @apiParam {String{30}} displayName 字段显示名.
     * @apiParam {String{30}} name 字段名称.
     * @apiParam {Number} fieldProperties 字段属性.
     * @apiParam {Number} dataType 数据类型.
     * @apiParam {String{20}} fieldWidth 字段宽度(非必须).
     * @apiParam {String{20}} fieldPrecision 字段精度(非必须).
     * @apiParam {Number} metadataStandardsId 元数据规范ID.
     * @apiParam {Number} defaultValue 默认值.
     * @apiParam {String{50}} definition 定义(非必须).
     * @apiParam {String{50}} objective 目的(非必须).
     * @apiParam {Number} constraint 约束性.
     * @apiParam {Number} elementType 元素类型.
     * @apiParam {String{30}} codingModification 编码修饰体系(非必须).
     * @apiParam {String{30}} relatedElements 相关元素(非必须).
     * @apiParam {String{30}} range 值域(非必须).
     * @apiParam {String{30}} informationSources 信息来源(非必须).
     * @apiParam {String{100}} remark 备注(非必须).
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
     * @apiParam {Number} id ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        List<DescriptionItem> descriptionItems = descriptionItemService.findByMetadataId(id);
        if(descriptionItems.size()!=0){
            throw new BusinessException("该元数据已被使用");
        }
        metadataService.delete(id);
    }

    /**
     * @api {put} /metadata 修改元数据
     * @apiName update
     * @apiGroup metadata
     * @apiParam {Number} id ID
     * @apiParam {String{30}} displayName 字段显示名.
     * @apiParam {String{30}} name 字段名称.
     * @apiParam {Number} fieldProperties 字段属性.
     * @apiParam {Number} dataType 数据类型.
     * @apiParam {String{20}} fieldWidth 字段宽度.
     * @apiParam {String{20}} fieldPrecision 字段精度.
     * @apiParam {Number} parentId 父字段ID.
     * @apiParam {Number} defaultValue 默认值.
     * @apiParam {String{50}} definition 定义.
     * @apiParam {String{50}} objective 目的.
     * @apiParam {Number} constraint 约束性.
     * @apiParam {Number} elementType 元素类型.
     * @apiParam {String{30}} codingModification 编码修饰体系.
     * @apiParam {String{30}} relatedElements 相关元素.
     * @apiParam {String{30}} range 值域.
     * @apiParam {String{30}} informationSources 信息来源.
     * @apiParam {String{100}} remark 备注.
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
     * @apiParam {Number} id 词典ID（url占位符）
     * @apiSuccess (Success 200) {Number} id ID.
     * @apiSuccess (Success 200) {String} displayName 字段显示名.
     * @apiSuccess (Success 200) {String} name 字段名称.
     * @apiSuccess (Success 200) {Number} fieldProperties 字段属性.
     * @apiSuccess (Success 200) {Number} dataType 数据类型.
     * @apiSuccess (Success 200) {String} fieldWidth 字段宽度.
     * @apiSuccess (Success 200) {String} fieldPrecision 字段精度.
     * @apiSuccess (Success 200) {Number} parentId 父字段ID.
     * @apiSuccess (Success 200) {Number} metadataStandardsId 元数据规范ID.
     * @apiSuccess (Success 200) {Number} defaultValue 默认值.
     * @apiSuccess (Success 200) {String} definition 定义.
     * @apiSuccess (Success 200) {String} objective 目的.
     * @apiSuccess (Success 200) {Number} constraint 约束性.
     * @apiSuccess (Success 200) {Number} elementType 元素类型.
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
     * @api {patch} /metadata/{upId},{downId}/priority 排序
     * @apiName priority
     * @apiGroup metadata
     * @apiParam {Number} upId 上移ID（url占位符）
     * @apiParam {Number} downId 下移ID（url占位符）
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        metadataService.priority(upId, downId);
    }
}
