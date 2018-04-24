package com.ztdx.eams.controller;

import com.ztdx.eams.domain.system.application.ResourceService;
import com.ztdx.eams.domain.system.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/resource")
public class ResourceController {

    private ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * @api {post} /resource 新增资源
     * @apiName save
     * @apiGroup resource
     * @apiParam {String} resourceName 资源名称
     * @apiParam {String} resourceUrl 资源url
     * @apiParam {long} parentId 上级节点id
     * @apiParam {String="dir","func"} resourceType 资源类型
     * @apiError (Error 400) message 1.资源路径已存在 2.上级节点id不存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Resource resource) {
        resourceService.save(resource);
    }

    /**
     * @api {delete} /resource/{id} 删除资源
     * @apiName delete
     * @apiGroup resource
     * @apiParam {long} id 资源id（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        resourceService.delete(id);
    }

    /**
     * @api {put} /resource/{id} 修改资源
     * @apiName update
     * @apiGroup resource
     * @apiParam {String} resourceName 资源名称
     * @apiParam {String} resourceUrl 资源url
     * @apiParam {long} parentId 上级节点id
     * @apiParam {String="dir","func"} resourceType 资源类型 dir:目录 func:功能
     * @apiError (Error 400) message 1.资源路径已存在 2.上级节点id不存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable("id") int id, @RequestBody Resource resource) {
        resource.setId(id);
        resourceService.update(resource);
    }

    /**
     * @api {get} /resource/{id} 获取资源详情
     * @apiName get
     * @apiGroup resource
     * @apiParam {long} id 资源id（url占位符）
     * @apiSuccess (Success 200) {int} id 资源id
     * @apiSuccess (Success 200) {String} resourceName 资源名称
     * @apiSuccess (Success 200) {String} resourceUrl 资源url
     * @apiSuccess (Success 200) {long} parentId 上级id
     * @apiSuccess (Success 200) {String="dir","func"} resourceType 资源类型 dir:目录 func:功能
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 2,"resourceName": "资源名称", "resourceUrl":"资源url","parentId": 1,"resourceType": "资源类型"}}
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Resource get(@PathVariable("id") long id) {
        return resourceService.getResource(id);
    }

    /**
     * @api {patch} /resource/{upId},{downId}/priority 修改资源排序优先级
     * @apiName priority
     * @apiGroup resource
     * @apiParam {int} upId 上移资源id（url占位符）
     * @apiParam {int} downId 下移资源id（url占位符）
     * @apiError (Error 400) message 1.不在一个节点上 2.资源不存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        resourceService.priority(upId, downId);
    }
}
