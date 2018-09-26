package com.ztdx.eams.controller.archives;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.exception.ForbiddenException;
import com.ztdx.eams.domain.archives.application.ClassificationService;
import com.ztdx.eams.domain.archives.model.Classification;
import com.ztdx.eams.domain.system.application.PermissionService;
import com.ztdx.eams.domain.system.application.RoleService;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * Created by li on 2018/4/18.
 */
@RestController
@RequestMapping(value = "/classification")
public class ClassificationController {

    private final ClassificationService classificationService;

    private final ArchivesQuery archivesQuery;


    private final RoleService roleService;

    private final PermissionService permissionService;

    @Autowired
    public ClassificationController(ClassificationService classificationService, ArchivesQuery archivesQuery, RoleService roleService, PermissionService permissionService) {
        this.classificationService = classificationService;
        this.archivesQuery = archivesQuery;
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    /**
     * @api {get} /classification/treeList 获取全宗所属档案分类表单树
     * @apiName treeList
     * @apiGroup classification
     * @apiParam {Number} fondsId 所属全宗ID(全局为1)(url参数)
     * @apiSuccess (Success 200) {Number} id 档案分类ID.
     * @apiSuccess (Success 200) {String} code 档案分类编码.
     * @apiSuccess (Success 200) {String} name 档案分类名称.
     * @apiSuccess (Success 200) {String} retentionPeriod 保管期限.
     * @apiSuccess (Success 200) {Number} parentId 上级档案分类ID.
     * @apiSuccess (Success 200) {Number} orderNumber 同级排序编号.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 档案分类ID,"code": "档案分类编码","name": "父档案分类0","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"orderNumber": 同级排序编号,"remark": "备注"},
     * {"id": 档案分类ID,"code": "档案分类编码","name": "父档案分类1","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"orderNumber": 同级排序编号,"remark": "备注","children": [
     * {"id": 档案分类ID,"code": "档案分类编码","name": "子档案分类1","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"orderNumber": 同级排序编号,"remark": "备注"}]}]}}.
     */
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList(@RequestParam("fondsId") int fondsId) {
        return archivesQuery.getClassificationTreeMap(UInteger.valueOf(fondsId));
    }

    /**
     * @api {get} /classification/fondsAndClassificationTreeList 著录项设置档案分类下拉树
     * @apiName fondsAndClassificationTreeList
     * @apiGroup classification
     * @apiSuccess (Success 200) {String} childrenType 节点类型(1.Classification-档案分类;2.Fonds-全宗).
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccess (Success 200) {Number} Fonds:id 全宗ID.
     * @apiSuccess (Success 200) {String} Fonds:code 全宗号.
     * @apiSuccess (Success 200) {String} Fonds:name 全宗名称.
     * @apiSuccess (Success 200) {Number} Fonds:parentId 上级全宗ID.
     * @apiSuccess (Success 200) {Number} Fonds:orderNumber 排序号.
     * @apiSuccess (Success 200) {Number} Fonds:type 全宗类型.
     * @apiSuccess (Success 200) {Number} Classification:id 档案分类ID.
     * @apiSuccess (Success 200) {String} Classification:code 档案分类编码.
     * @apiSuccess (Success 200) {String} Classification:name 档案分类名称.
     * @apiSuccess (Success 200) {Number} Classification:parentId 上级档案分类ID.
     * @apiSuccess (Success 200) {Number} Classification:orderNumber 同级排序编号.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"item": [{"id": 11,"code": "档案分类编码","name": "档案分类名称","parentId": 1,"orderNumber": 同级排序编号},
     * {"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 2,"orderNumber": 排序号,"type": 全宗类型,"children": [
     * {"id": 12,"code": "档案分类编码","name": "档案分类名称","parentId": 3,"orderNumber": 同级排序编号,"children": [
     * {"id": 14,"code": "档案分类编码","name": "档案分类名称","parentId": 4,"orderNumber": 同级排序编号}]}]}]}}
     */
    @RequestMapping(value = "/fondsAndClassificationTreeList", method = RequestMethod.GET)
    public Map<String, Object> treeList(@SessionAttribute(required = false) UserCredential LOGIN_USER) {
        int userId;
        if (LOGIN_USER != null) {
            userId = LOGIN_USER.getUserId();
        } else {
            throw new ForbiddenException("拒绝访问");
        }
        Set<Integer> fondsIds = roleService.findUserManageFonds(userId);
        return archivesQuery.getFondsAndClassificationTreeMap(a -> hasPermission(fondsIds, a));
    }

    private boolean hasPermission(Set<Integer> ids, int id) {
        if (permissionService.hasAnyAuthority("ROLE_ADMIN")) {
            return true;
        } else {
            return ids.contains(id);
        }

    }
    /**
     * @api {get} /classification/treeListByParentId 通过上级档案分类节点获取档案分类下拉树
     * @apiName treeListByParentId
     * @apiGroup classification
     * @apiParam {Number} parentId 父档案分类ID(根节点为1)(url参数)
     * @apiSuccess (Success 200) {Number} id 档案分类ID.
     * @apiSuccess (Success 200) {String} code 档案分类编码.
     * @apiSuccess (Success 200) {String} name 档案分类名称.
     * @apiSuccess (Success 200) {String} retentionPeriod 保管期限.
     * @apiSuccess (Success 200) {Number} parentId 上级档案分类ID.
     * @apiSuccess (Success 200) {Number} orderNumber 同级排序编号.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 2,"code": "档案分类编码","name": "父档案分类0","retentionPeriod": "保管期限","parentId": 1,"orderNumber": 1,"remark": "备注"},
     * {"id": 3,"code": "档案分类编码","name": "父档案分类1","retentionPeriod": "保管期限","parentId": 1,"orderNumber": 2,"remark": "备注","children": [
     * {"id": 4,"code": "档案分类编码","name": "子档案分类1","retentionPeriod": "保管期限","parentId": 1,"orderNumber": 3,"remark": "备注"}]}]}}.
     */
    @RequestMapping(value = "/treeListByParentId", method = RequestMethod.GET)
    public Map<String, Object> treeListByParentId(@RequestParam("parentId") int parentId) {
        return archivesQuery.getClassificationTreeMapByParent(UInteger.valueOf(parentId));
    }

    /**
     * @api {post} /classification 新增档案分类
     * @apiName save
     * @apiGroup classification
     * @apiParam {Number} parentId 上级档案分类ID（根节点传1）
     * @apiParam {String} code 档案分类编码
     * @apiParam {String} name 档案分类名称
     * @apiParam {String} fondsId 所属全宗ID
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiParam {String} retentionPeriod 保管期限
     * @apiError (Error 400) message 档案分类编码已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Classification classification) {
        classificationService.save(classification);
    }

    /**
     * @api {delete} /classification/{id} 删除档案分类
     * @apiName delete
     * @apiGroup classification
     * @apiParam {Number} id 档案分类ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        classificationService.delete(id);
    }

    /**
     * @api {put} /classification 修改档案分类信息
     * @apiName update
     * @apiGroup classification
     * @apiParam {Number} id 档案分类ID
     * @apiParam {Number} parentId 上级档案分类ID（根节点传0）
     * @apiParam {String{30}} code 档案分类编码
     * @apiParam {String{30}} name 档案分类名称
     * @apiParam {String{100}} remark 备注（未输入传""值）
     * @apiParam {String{20}} retentionPeriod 保管期限
     * @apiError (Error 400) message 档案分类编码已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Classification classification) {
        classificationService.update(classification);
    }

    /**
     * @api {get} /classification/{id} 获取档案分类详情
     * @apiName get
     * @apiGroup classification
     * @apiParam {Number} id 档案分类ID（url占位符）
     * @apiSuccess (Success 200) {Number} id 档案分类ID.
     * @apiSuccess (Success 200) {String} code 档案分类编码.
     * @apiSuccess (Success 200) {String} name 档案分类名称.
     * @apiSuccess (Success 200) {String} retentionPeriod 保管期限.
     * @apiSuccess (Success 200) {Number} parentId 上级档案分类ID.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 档案分类ID,"code": "档案分类编码","name": "档案分类名称","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return archivesQuery.getClassification(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /classification/{upId},{downId}/priority 档案分类排序
     * @apiName priority
     * @apiGroup classification
     * @apiParam {Number} upId 上移档案分类ID（url占位符）
     * @apiParam {Number} downId 下移档案分类ID（url占位符）
     * @apiError (Error 400) message 上级档案分类不一致.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        classificationService.priority(upId, downId);
    }
}
