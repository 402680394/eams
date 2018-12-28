package com.ztdx.eams.controller.store;

import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.store.application.MonitoringPointService;
import com.ztdx.eams.domain.store.application.StorageService;
import com.ztdx.eams.domain.store.model.MonitoringPoint;
import com.ztdx.eams.domain.store.model.Storage;
import com.ztdx.eams.query.StoreQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     * @apiParam {Number} storageId 所属库房id(url参数)
     * @apiParam {String} keyWord 检索框输入的内容(非必须)(url参数)
     * @apiParam {Number} pageNum 每页条数（默认为15）（url参数）
     * @apiParam {Number} pageSize 页码（默认为1）（url参数）
     * @apiSuccess (Success 200) {Number} id 库房ID.
     * @apiSuccess (Success 200) {String} name 库房名称.
     * @apiSuccess (Success 200) {String} number 库房编号.
     * @apiSuccess (Success 200) {String} description 库房描述.
     * @apiSuccess (Success 200) {Number} fondsId 所属全宗id.
     * @apiSuccess (Success 200) {Number} total 数据总数
     * @apiSuccessExample {json} Success-Response:
     * {
     * "data":{
     * "items":[{
     *            "id": 2,
     *            "name": "mm",
     *            "number": "BJ_01",
     *            "description": "傻大葱",
     *            "fonds_id": "2"
     *       }]
     * "total":1
     * }
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_read')")
    @RequestMapping(value = "/storageList", method = RequestMethod.GET)
    public Map<String,Object> storageList(@RequestParam(value = "keyWord", defaultValue = "") String keyWord
            , @RequestParam(name = "pageSize", required = false) Integer pageSize
            , @RequestParam(name = "pageNum", required = false) Integer pageNum) {
        return storeQuery.getStorageList(keyWord.trim(), pageNum, pageSize);
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
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
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
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "/updateStorage", method = RequestMethod.PUT)
    public void update(@RequestBody Storage storage) {
        storageService.update(storage);
    }

    /**
     * @api {delete} /store/deleteStorage 删除库房
     * @apiName deleteStorage
     * @apiGroup store
     * @apiParam {Array} storageIds 库房ID数组
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "/deleteStorage", method = RequestMethod.DELETE)
    public void deleteStorage(@JsonParam List<Integer> storageIds) {
        storageService.delete(storageIds);
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
     * @apiError (Error 400) message 监测点编号已存在.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "/saveMonitoringPoint", method = RequestMethod.POST)
    public void saveMonitoringPoint(@RequestBody MonitoringPoint monitoringPoint) {
        monitoringPointService.save(monitoringPoint);
    }

    /**
     * @api {put} /store/updateMonitoringPoint 修改监测点
     * @apiName updateMonitoringPoint
     * @apiGroup store
     * @apiParam {Number} id 监测点id.
     * @apiParam {Number} storageId 所属库房id.
     * @apiParam {String{10}} number 监测点编号.
     * @apiParam {Number} type 监测点类型（1 温度记录仪 2 湿度记录仪 3 温湿度记录仪）.
     * @apiParam {Number} status 监测点状态(0表示否，1表示是).
     * @apiParam {String{100}} remark 备注.
     * @apiError (Error 400) message 监测点编号已存在.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "/updateMonitoringPoint", method = RequestMethod.PUT)
    public void updateMonitoringPoint(@RequestBody MonitoringPoint monitoringPoint) {
        monitoringPointService.update(monitoringPoint);
    }

    /**
     * @api {delete} /store/deleteMonitoringPoint 删除监测点
     * @apiName deleteMonitoringPoint
     * @apiGroup store
     * @apiParam {Array} monitoringPointIds 监测点ID数组
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "/deleteMonitoringPoint", method = RequestMethod.DELETE)
    public void deleteMonitoringPoint(@JsonParam List<Integer> monitoringPointIds) {
        monitoringPointService.delete(monitoringPointIds);
    }


    /**
     * @api {get} /store/monitoringPointList 监测点表单列表
     * @apiName monitoringPointList
     * @apiGroup store
     * @apiParam {Number} storageId 库房ID(url参数)
     * @apiParam {String} keyWord 检索框输入的内容(未输入传""值)(url参数)
     * @apiParam {Number} pageNum 每页条数（默认为15）（url参数）
     * @apiParam {Number} pageSize 页码（默认为1）（url参数）
     * @apiSuccess (Success 200) {Number} id 监测点id.
     * @apiSuccess (Success 200) {Number} storageId 所属库房id.
     * @apiSuccess (Success 200) {String{10}} number 监测点编号.
     * @apiSuccess (Success 200) {Number} type 监测点类型（1 温度记录仪 2 湿度记录仪 3 温湿度记录仪）.
     * @apiSuccess (Success 200) {Number} status 监测点状态(0表示否，1表示是).
     * @apiSuccess (Success 200) {String{100}} remark 备注.
     * @apiSuccess (Success 200) {Number} fondsId 所属全宗id.
     * @apiSuccess (Success 200) {Number} total 数据总数
     * @apiSuccessExample {json} Success-Response:
     * {
     * "data":{
     * "items":[
     * {
     *         "id": 1,
     *         "storage_id": 1,
     *         "number": "BH001",
     *         "type": 1,
     *         "status": 0,
     *         "remark": "beizhu",
     *         "name": "cc"
     * }
     *       ]
     * "total":1
     * }
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_read')")
    @RequestMapping(value = "/monitoringPointList", method = RequestMethod.GET)
    public Map<String,Object> monitoringPointList(@RequestParam("storageId") Integer storageId
            , @RequestParam(value = "keyWord", defaultValue = "") String keyWord
            , @RequestParam(name = "pageSize", required = false, defaultValue = "15") int pageSize
            , @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum) {
        return storeQuery.getMonitoringPointList(storageId,keyWord, pageNum, pageSize);
    }

    /**
     * @api {get} /store/monitoringPointTypeList 监测点类型下拉列表
     * @apiName selectList
     * @apiGroup store
     * @apiParam {Number} storageId 库房ID(url参数)
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
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_read')")
    @RequestMapping(value = "/monitoringPointTypeList", method = RequestMethod.GET)
    public Map<String,Object> monitoringPointType(@RequestParam("storageId") Integer storageId) {
        return storeQuery.getMonitoringPointType(storageId);
    }



}
