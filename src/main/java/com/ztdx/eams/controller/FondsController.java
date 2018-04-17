package com.ztdx.eams.controller;

import com.ztdx.eams.domain.system.application.FondsService;
import com.ztdx.eams.domain.system.model.Fonds;
import com.ztdx.eams.query.SystemQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by li on 2018/4/15.
 */
@RestController
@RequestMapping(value = "/fonds")
public class FondsController {

    private final FondsService fondsService;

    private final SystemQuery systemQuery;

    @Autowired
    public FondsController(FondsService fondsService, SystemQuery systemQuery) {
        this.fondsService = fondsService;
        this.systemQuery = systemQuery;
    }

    /**
     * @api {get} /fonds/list 获取全宗列表
     * @apiName list
     * @apiGroup fonds
     * @apiSuccess (Success 200) {int} id 全宗ID.
     * @apiSuccess (Success 200) {String} code 全宗号.
     * @apiSuccess (Success 200) {String} name 全宗名称.
     * @apiSuccess (Success 200) {int} parentId 上级全宗ID.
     * @apiSuccess (Success 200) {int} orderNumber 同级排序编号.
     * @apiSuccess (Success 200) {int} type 全宗类型
     * @apiSuccess (Success 200) {arr} subFonds 子全宗信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 全宗ID,"code": "全宗号","name": "父全宗0","parentId": 上级全宗ID,"orderNumber": 同级排序编号,"type": 全宗类型},
     * {"id": 全宗ID,"code": "全宗号","name": "父全宗1","parentId": 上级全宗ID,"orderNumber": 同级排序编号,"type": 全宗类型,"subFonds": [
     * {"id": 全宗ID,"code": "全宗号","name": "子全宗1","parentId": 上级全宗ID,"orderNumber": 同级排序编号,"type": 全宗类型,}]}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list() {
        return systemQuery.getFondsListMap(UInteger.valueOf(0));
    }

    /**
     * @api {post} /fonds 新增全宗
     * @apiName save
     * @apiGroup fonds
     * @apiParam {int} parentId 上级全宗ID（根机构传0）
     * @apiParam {String} code 全宗号
     * @apiParam {String} name 全宗名称
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiParam {int} type 全宗类型
     * @apiError (Error 400) message 全宗号已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Fonds fonds) {
        fondsService.save(fonds);
    }

    /**
     * @api {delete} /fonds/{id} 删除全宗
     * @apiName delete
     * @apiGroup fonds
     * @apiParam {int} id 全宗ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        fondsService.delete(id);
    }

    /**
     * @api {put} /fonds 修改全宗信息
     * @apiName update
     * @apiGroup fonds
     * @apiParam {int} id 全宗ID
     * @apiParam {int} parentId 上级全宗ID（根机构传0）
     * @apiParam {String} code 全宗号
     * @apiParam {String} name 全宗名称
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiParam {String} type 全宗类型
     * @apiError (Error 400) message 全宗号已存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Fonds fonds) {
        fondsService.update(fonds);
    }

    /**
     * @api {get} /fonds/{id} 获取全宗详情
     * @apiName get
     * @apiGroup fonds
     * @apiParam {int} id 全宗ID（url占位符）
     * @apiSuccess (Success 200) {int} id 全宗ID.
     * @apiSuccess (Success 200) {String} code 全宗号.
     * @apiSuccess (Success 200) {String} name 全宗名称.
     * @apiSuccess (Success 200) {int} parentId 上级全宗ID.
     * @apiSuccess (Success 200) {String} type 全宗类型
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 全宗ID,"code": "全宗编码","name": "全宗名称","parentId": 上级全宗ID,"type": "全宗类型"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return systemQuery.getFonds(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /fonds/{upId},{downId}/priority 修改全宗排序优先级
     * @apiName priority
     * @apiGroup fonds
     * @apiParam {int} upId 上移全宗ID（url占位符）
     * @apiParam {int} downId 下移全宗ID（url占位符）
     * @apiError (Error 400) message 上级全宗不一致.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        fondsService.priority(upId, downId);
    }
}
