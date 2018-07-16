package com.ztdx.eams.controller.store;

import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.store.application.BoxService;
import com.ztdx.eams.domain.store.model.Box;
import com.ztdx.eams.query.StoreQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/7/5.
 */
@RestController
@RequestMapping(value = "box")
public class BoxController {

    private final StoreQuery storeQuery;

    private final BoxService boxService;

    @Autowired
    public BoxController(StoreQuery storeQuery, BoxService boxService) {
        this.storeQuery = storeQuery;
        this.boxService = boxService;
    }

    /**
     * @api {get} /box/list?archivesId={archivesId}&code={code}&status={status}&onFrame={onFrame} 档案盒表单列表
     * @apiName list
     * @apiGroup box
     * @apiParam {Number} archivesId 所属库ID(url参数)
     * @apiParam {String{30}} code 盒号(未输入传""值)(url参数)
     * @apiParam {Number} status 容纳状况(0-全部，1-已满，2-未满)(url参数)
     * @apiParam {Number} onFrame 是否在架(0-全部，1-已上架，2-未上架)(url参数)
     * @apiSuccess (Success 200) {String} code 盒号.
     * @apiSuccess (Success 200) {Number} filesTotal 文件份数.
     * @apiSuccess (Success 200) {Number} pagesTotal 文件页数.
     * @apiSuccess (Success 200) {Number} maxPagesTotal 最大容量（页）.
     * @apiSuccess (Success 200) {Number} width 盒子宽度（毫米）.
     * @apiSuccess (Success 200) {Boolean} onFrame 是否在架.
     * @apiSuccess (Success 200) {String} point 位置编码.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"code": 盒号,"filesTotal": 文件份数,"pagesTotal": 文件页数,"maxPagesTotal": 容纳最大页数,"width": 盒子宽度,"onStand": 是否在架,"point": "位置编码","remark": "备注"}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list(@RequestParam("archivesId") int archivesId, @RequestParam("code") String code, @RequestParam("status") int status, @RequestParam("onFrame") int onFrame) {
        return storeQuery.getBoxList(archivesId, code, status, onFrame);
    }

    /**
     * @api {post} /box/ 新增档案盒
     * @apiName save
     * @apiGroup box
     * @apiParam {Number} archivesId 档案库ID
     * @apiParam {String} codeRule 盒号规则(没有传"")
     * @apiParam {String} flowNumber 流水号
     * @apiParam {Number} total 新增个数
     * @apiParam {Number} maxPagesTotal 最大容量（页）
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 盒号已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void save(@RequestBody Box box, @JsonParam int total) {
        boxService.save(box, total);
    }

    /**
     * @api {delete} /box/ 删除档案盒
     * @apiName delete
     * @apiGroup box
     * @apiParam {Number} archivesId 档案库ID
     * @apiParam {Number[]} ids 盒ID
     * @apiError (Error 400) message 1.需要先下架在架档案盒.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public void delete(@RequestBody Map<String, Object> map) {
        // TODO: 拆盒
        int archivesId = (int) map.get("archivesId");
        List<Integer> ids = (List<Integer>) map.get("ids");
        boxService.delete(ids);
    }

    /**
     * @api {put} /box 修改档案盒信息
     * @apiName update
     * @apiGroup box
     * @apiParam {Number} id 盒ID
     * @apiParam {String{100}} codeRule 盒号规则(没有传"")
     * @apiParam {String{100}} flowNumber 流水号
     * @apiParam {Number} width 盒子宽度（毫米）
     * @apiParam {Number} maxPagesTotal 最大容量（页）
     * @apiParam {String{50}} remark 备注（未输入传""值）
     * @apiError (Error 400) message 1.盒号已存在；2.已有容量超出，请先拆盒.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Box box) {
        boxService.update(box);
    }

    /**
     * @api {get} /box/{id} 获取档案盒信息
     * @apiName get
     * @apiGroup box
     * @apiParam {Number} id 盒ID（url占位符）
     * @apiSuccess (Success 200) {Number} id 盒ID.
     * @apiSuccess (Success 200) {String} codeRule 盒号规则.
     * @apiSuccess (Success 200) {String} flowNumber 流水号.
     * @apiSuccess (Success 200) {Number} width 盒子宽度（毫米）.
     * @apiSuccess (Success 200) {Number} maxPagesTotal 最大容量（页）
     * @apiSuccess (Success 200) {String} remark 备注（未输入传""值）
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 盒ID,"code": "盒号","width": 盒子宽度,"maxPagesTotal": 最大容量,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return storeQuery.getBox(UInteger.valueOf(id));
    }

    /**
     * @api {put} /box/onFrame 上架
     * @apiName onFrame
     * @apiGroup box
     * @apiParam {String} cellCode 密集架格的编码
     * @apiParam {Number[]} ids 盒ID
     */
    @RequestMapping(value = "/onFrame", method = RequestMethod.PUT)
    public void onFrame(@RequestBody Map<String, Object> map) {
        String cellCode = (String) map.get("cellCode");
        List<Integer> ids = (List<Integer>) map.get("ids");
        boxService.onFrame(cellCode, ids);
    }

    /**
     * @api {put} /box/downFrame 下架
     * @apiName downFrame
     * @apiGroup box
     * @apiParam {Number[]} ids 盒ID
     */
    @RequestMapping(value = "/downFrame", method = RequestMethod.PUT)
    public void downFrame(@RequestBody List<Integer> ids) {
        boxService.downFrame(ids);
    }

    /**
     * @api {get} /box/maxFlowNumber 获取流水号最大值
     * @apiName maxFlowNumber
     * @apiGroup box
     * @apiParam {Number} archivesId 档案库ID（url参数）
     * @apiParam {String} codeRule 盒号规则（无规则时传""值）（url参数）
     * @apiSuccess (Success 200) {String} maxFlowNumber 流水号最大值.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"maxFlowNumber": 流水号最大值}}.
     */
    @RequestMapping(value = "/maxFlowNumber", method = RequestMethod.GET)
    public Map<String, Object> maxFlowNumber(@RequestParam("archivesId") int archivesId, @RequestParam("codeRule") String codeRule) {
        return storeQuery.maxFlowNumber(archivesId, codeRule);
    }

}
