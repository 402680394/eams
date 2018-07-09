package com.ztdx.eams.controller.archives;

import com.ztdx.eams.domain.archives.application.MetadataStandardsService;
import com.ztdx.eams.domain.archives.model.MetadataStandards;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by li on 2018/4/22.
 */
@RestController
@RequestMapping(value = "/metadataStandards")
public class MetadataStandardsController {

    private final MetadataStandardsService metadataStandardsService;

    private final ArchivesQuery archivesQuery;
    @Autowired
    public MetadataStandardsController(MetadataStandardsService metadataStandardsService, ArchivesQuery archivesQuery) {
        this.metadataStandardsService = metadataStandardsService;
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /metadataStandards/list 获取元数据规范列表
     * @apiName list
     * @apiGroup metadataStandards
     * @apiParam {String{30}} name 名称(未输入传""值)(url参数)
     * @apiSuccess (Success 200) {Number} id ID.
     * @apiSuccess (Success 200) {String} code 编号.
     * @apiSuccess (Success 200) {String} name 名称.
     * @apiSuccess (Success 200) {Number} characterSet 字符集(1-"utf-8",2-"gb2312",3-其他).
     * @apiSuccess (Success 200) {String} releaseOrganization 发布机构.
     * @apiSuccess (Success 200) {String} descriptionFile 描述文件.
     * @apiSuccess (Success 200) {String} edition 版本.
     * @apiSuccess (Success 200) {Number} orderNumber 排序号.
     * @apiSuccess (Success 200) {Number} flag 是否启用(0-启用,2-禁用).
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": ID,"code": "编号","name": "名称","characterSet": 字符集,"releaseOrganization": "发布机构","descriptionFile": "描述文件","edition": "版本","orderNumber": 排序号,"flag": 是否启用,"remark": "备注"}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list(@RequestParam(value = "name",defaultValue = "") String name) {
        return archivesQuery.getMetadataStandardsList(name);
    }

    /**
     * @api {post} /metadataStandards 新增元数据规范
     * @apiName save
     * @apiGroup metadataStandards
     * @apiParam {String{30}} code 编号
     * @apiParam {String{30}} name 名称
     * @apiParam {Number} characterSet 字符集(1-"utf-8",2-"gb2312",3-其他).
     * @apiParam {String{20}} releaseOrganization 发布机构（未输入传""值）
     * @apiParam {String{30}} descriptionFile 描述文件（未输入传""值）
     * @apiParam {String{20}} edition 版本.
     * @apiParam {Number{0-1}} flag 是否启用(0-启用,1-禁用).
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 编号已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody MetadataStandards metadataStandards) {
        metadataStandardsService.save(metadataStandards);
    }

    /**
     * @api {delete} /metadataStandards/{id} 删除元数据规范
     * @apiName delete
     * @apiGroup metadataStandards
     * @apiParam {Number} id ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        metadataStandardsService.delete(id);
    }

    /**
     * @api {put} /metadataStandards 修改元数据规范
     * @apiName update
     * @apiGroup metadataStandards
     * @apiParam {Number} id ID
     * @apiParam {String{30}} code 编号
     * @apiParam {String{30}} name 名称
     * @apiParam {Number} characterSet 字符集(1-"utf-8",2-"gb2312",3-其他).
     * @apiParam {String{20}} releaseOrganization 发布机构（未输入传""值）
     * @apiParam {String{30}} descriptionFile 描述文件（未输入传""值）
     * @apiParam {String{20}} edition 版本.
     * @apiParam {Number{0-1}} flag 是否启用(0-启用,1-禁用).
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 编号已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody MetadataStandards metadataStandards) {
        metadataStandardsService.update(metadataStandards);
    }

    /**
     * @api {get} /metadataStandards/{id} 获取元数据规范详情
     * @apiName get
     * @apiGroup metadataStandards
     * @apiParam {Number} id 词典ID（url占位符）
     * @apiSuccess (Success 200) {Number} id ID.
     * @apiSuccess (Success 200) {String} code 编号.
     * @apiSuccess (Success 200) {String} name 名称.
     * @apiSuccess (Success 200) {Number} characterSet 字符集(1-"utf-8",2-"gb2312",3-其他).
     * @apiSuccess (Success 200) {String} releaseOrganization 发布机构.
     * @apiSuccess (Success 200) {String} descriptionFile 描述文件.
     * @apiSuccess (Success 200) {String} edition 版本.
     * @apiSuccess (Success 200) {Number} orderNumber 排序号.
     * @apiSuccess (Success 200) {Number} flag 是否启用(0-启用,2-禁用).
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": ID,"code": "编号","name": "名称","characterSet": 字符集,"releaseOrganization": "发布机构","descriptionFile": "描述文件","edition": "版本","orderNumber": 排序号,"flag": 是否启用,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return archivesQuery.getMetadataStandards(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /metadataStandards/{upId},{downId}/priority 修改元数据规范排序优先级
     * @apiName priority
     * @apiGroup metadataStandards
     * @apiParam {Number} upId 上移词典ID（url占位符）
     * @apiParam {Number} downId 下移词典ID（url占位符）
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        metadataStandardsService.priority(upId, downId);
    }
}
