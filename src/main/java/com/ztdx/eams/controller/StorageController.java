package com.ztdx.eams.controller;

import com.ztdx.eams.domain.store.application.MonitoringPointService;
import com.ztdx.eams.domain.store.application.StorageService;
import com.ztdx.eams.domain.store.model.MonitoringPoint;
import com.ztdx.eams.domain.store.model.Storage;
import com.ztdx.eams.query.StoreQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping(value = "/store")
public class StorageController {


    private final StorageService storageService;

    private final MonitoringPointService monitoringPointService;

    private final StoreQuery storeQuery;

    /**
     * 构造函数
     */
    @Autowired
    public StorageController(StorageService storageService, MonitoringPointService monitoringPointService, StoreQuery storeQuery) {
        this.storageService = storageService;
        this.monitoringPointService = monitoringPointService;
        this.storeQuery = storeQuery;
    }

    /**
     * @api {get} /store/storageList 库房表单列表
     * @apiName storageList
     * @apiGroup store
     * @apiParam {Number} fondsId 全宗ID(url参数)
     * @apiParam {String} keyWord 检索框输入的内容(未输入传""值)(url参数)
     * @apiSuccess (Success 200) {Number} id 库房ID.
     * @apiSuccess (Success 200) {String} name 库房名称.
     * @apiSuccess (Success 200) {String} number 库房编号.
     * @apiSuccess (Success 200) {String} description 库房描述.
     * @apiSuccess (Success 200) {Number} fondsId 所属全宗id.
     * @apiSuccessExample {json} Success-Response:
     * {
     *      "id": 2,
     *      "name": "mm",
     *      "number": "BJ_01",
     *      "description": "聪聪最漂亮",
     *      "fonds_name": "张聪聪"
     * }
     */
    @RequestMapping(value = "/storageList", method = RequestMethod.GET)
    public Map<String,Object> storageList(@RequestParam(value = "keyWord", defaultValue = "") String keyWord) {
        if (keyWord!=null && !keyWord.trim().equals("")){
            return storeQuery.getStorageListByFondsIdAndKeyWord(keyWord);
        }
        return storeQuery.getStorageList();
    }

    /**
     * @api {post} /store/saveStorage 新增库房
     * @apiName saveStorage
     * @apiGroup store
     * @apiParam {String{20}} name 库房名称.
     * @apiParam {String{10}} number 库房编号.
     * @apiParam {String{100}} description 库房描述.
     * @apiParam {Number} fondsId 所属全宗id.
     * @apiError (Error 400) message 库房编号已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/saveStorage", method = RequestMethod.POST)
    public void save(@RequestBody Storage storage) {
        storageService.save(storage);
    }

    /**
     * @api {put} /store/updateStorage 修改库房信息
     * @apiName updateStorage
     * @apiGroup store
     * @apiParam {Number} id 库房ID.
     * @apiParam {String{20}} name 库房名称.
     * @apiParam {String{10}} number 库房编号.
     * @apiParam {String{100}} description 库房描述.
     * @apiParam {Number} fondsId 所属全宗id.
     * @apiError (Error 400) message 库房编号已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/updateStorage", method = RequestMethod.PUT)
    public void update(@RequestBody Storage storage) {
        storageService.update(storage);
    }

    /**
     * @api {delete} /store/{id} 删除库房
     * @apiName delete
     * @apiGroup store
     * @apiParam {Number} id 库房ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        storageService.delete(id);
    }


    /**
     * @api {post} /store/saveMonitoringPoint 新增监测点
     * @apiName saveMonitoringPoint
     * @apiGroup store
     * @apiParam {Number} storageId 所属库房id.
     * @apiParam {String{10}} number 监测点编号.
     * @apiParam {Number} type 监测点类型（1 温度记录仪 2 湿度记录仪 3 温湿度记录仪）.
     * @apiParam {Number} status 监测点状态(0表示否，1表示是).
     * @apiParam {String{100}} remark 备注.
     * @apiParam {Number} fondsId 所属全宗id.
     * @apiError (Error 400) message 监测点编号已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/saveMonitoringPoint", method = RequestMethod.POST)
    public void saveMonitoringPoint(@RequestBody MonitoringPoint monitoringPoint) {
        monitoringPointService.save(monitoringPoint);
    }

    /**
     * @api {post} /store/updateMonitoringPoint 修改监测点
     * @apiName updateMonitoringPoint
     * @apiGroup store
     * @apiParam {Number} id 监测点id.
     * @apiParam {Number} storageId 所属库房id.
     * @apiParam {String{10}} number 监测点编号.
     * @apiParam {Number} type 监测点类型（1 温度记录仪 2 湿度记录仪 3 温湿度记录仪）.
     * @apiParam {Number} status 监测点状态(0表示否，1表示是).
     * @apiParam {String{100}} remark 备注.
     * @apiParam {Number} fondsId 所属全宗id.
     * @apiError (Error 400) message 监测点编号已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/updateMonitoringPoint", method = RequestMethod.PUT)
    public void updateMonitoringPoint(@RequestBody MonitoringPoint monitoringPoint) {
        monitoringPointService.update(monitoringPoint);
    }

    /**
     * @api {delete} /store/deleteMonitoringPoint{id} 删除监测点
     * @apiName deleteMonitoringPoint
     * @apiGroup store
     * @apiParam {Number} id 库房ID（url占位符）
     */
    @RequestMapping(value = "/deleteMonitoringPoint{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") Integer id) {
        monitoringPointService.delete(id);
    }


    /**
     * @api {get} /store/monitoringPointList 监测点表单列表
     * @apiName monitoringPointList
     * @apiGroup store
     * @apiParam {Number} storageId 库房ID(url参数)
     * @apiParam {String} keyWord 检索框输入的内容(未输入传""值)(url参数)
     * @apiSuccess (Success 200) {Number} id 监测点id.
     * @apiSuccess (Success 200) {Number} storageId 所属库房id.
     * @apiSuccess (Success 200) {String{10}} number 监测点编号.
     * @apiSuccess (Success 200) {Number} type 监测点类型（1 温度记录仪 2 湿度记录仪 3 温湿度记录仪）.
     * @apiSuccess (Success 200) {Number} status 监测点状态(0表示否，1表示是).
     * @apiSuccess (Success 200) {String{100}} remark 备注.
     * @apiSuccess (Success 200) {Number} fondsId 所属全宗id.
     * @apiSuccessExample {json} Success-Response:
     * {
     *         "id": 1,
     *         "storage_id": 1,
     *         "number": "BH001",
     *         "type": 1,
     *         "status": 0,
     *         "remark": null,
     *         "name": "cc"
     * }
     */
    @RequestMapping(value = "/monitoringPointList", method = RequestMethod.GET)
    public Map<String,Object> monitoringPointList(@RequestParam("storageId") Integer storageId, @RequestParam(value = "keyWord", defaultValue = "") String keyWord) {
        return storeQuery.getMonitoringPointList(storageId,keyWord);
    }

    /**
     * @api {get} /store/monitoringPointTypeList 监测点类型下拉列表
     * @apiName selectList
     * @apiGroup store
     * @apiSuccess (Success 200) {Number} id 监测点ID.
     * @apiSuccess (Success 200) {String} number 监测点编号.
     * @apiSuccess (Success 200) {Number} type 监测点类型（1 温度记录仪 2 湿度记录仪 3 温湿度记录仪）.
     * @apiSuccessExample {json} Success-Response:
     * {
     *         "id": 1,
     *         "number": "BH001",
     *         "type": 1
     * }
     */
    @RequestMapping(value = "/monitoringPointTypeList", method = RequestMethod.GET)
    public Map<String,Object> monitoringPointType() {
        return storeQuery.getMonitoringPointType();
    }



}
