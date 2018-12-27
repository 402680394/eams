package com.ztdx.eams.controller.store;

import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.store.application.MonitoringRecordService;
import com.ztdx.eams.domain.store.model.MonitoringRecord;
import com.ztdx.eams.query.StoreQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/monitoringRecord")
public class MonitoringRecordController {


    private final MonitoringRecordService monitoringRecordService;

    private final StoreQuery storeQuery;

    @Autowired
    public MonitoringRecordController(MonitoringRecordService monitoringRecordService,StoreQuery storeQuery) {
        this.monitoringRecordService = monitoringRecordService;
        this.storeQuery = storeQuery;
    }


    /**
     * @api {get} /monitoringRecord/monitoringRecordList 监测记录列表
     * @apiName monitoringRecordList
     * @apiGroup monitoringRecord
     * @apiParam {Number} storageId 库房ID(url参数)
     * @apiParam {String} keyWord 检索框输入的内容(非必传)(url参数)
     * @apiParam {Number} pageNum 每页条数（默认为15）（url参数）
     * @apiParam {Number} pageSize 页码（默认为1）（url参数）
     * @apiSuccess (Success 200) {Number} id 监测记录ID.
     * @apiSuccess (Success 200) {Number} monitoringPointId 监测点id.
     * @apiSuccess (Success 200) {String} number 监测点编号.
     * @apiSuccess (Success 200) {Timestamp} monitoringTime 监测时间.
     * @apiSuccess (Success 200) {Number} temperatureValue 温度值.(最大100，最小-100)
     * @apiSuccess (Success 200) {Number} humidityValue 湿度值.(最大100，最小-100)
     * @apiSuccess (Success 200) {String{100}} takeSteps 采取措施.
     * @apiSuccess (Success 200) {Number} storageId 所属库房id.
     * @apiSuccess (Success 200) {Number} type 监测点类型（1 温度记录仪 2 湿度记录仪 3 温湿度记录仪）.
     * @apiSuccessExample {json} Success-Response:
     * {
     *      "id": 1,
     *      "number": "BH001",
     *      "monitoring_point_id": 1,
     *      "monitoring_time": 1528684108,
     *      "temperature_value": 100,
     *      "humidity_value": 0,
     *      "take_steps": "降温",
     *      "storage_id": 1,
     *      "type" : 2
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_read')")
    @RequestMapping(value = "/monitoringRecordList", method = RequestMethod.GET)
    public Map<String, Object> monitoringRecordList(@RequestParam(name = "storageId",required = false) Integer storageId
            , @RequestParam(value = "keyWord",required = false, defaultValue = "") String keyWord
            , @RequestParam(name = "pageSize", required = false, defaultValue = "15") int pageSize
            , @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum) {
        return storeQuery.getMonitoringRecordList(null == storageId ? null: UInteger.valueOf(storageId),keyWord, pageNum, pageSize);
    }

    /**
     * @api {post} /monitoringRecord 新增监测记录
     * @apiName save
     * @apiGroup monitoringRecord
     * @apiParam {Number} monitoringPointId 监测点ID.
     * @apiParam {Timestamp} monitoringTime 监测时间.
     * @apiParam {Number} temperatureValue 温度值.(最大100，最小-100,如果该监测点类型是温度记录仪，则温度值不能为空且不显示湿度值；如果类型是温湿度记录仪则不能为空且温湿度都显示)
     * @apiParam {Number} humidityValue 湿度值.(最大100，最小-100，如果该监测点类型是湿度记录仪，则湿度值不能为空且不显示温度值；如果类型是温湿度记录仪则不能为空且温湿度都显示)
     * @apiParam {String{100}} takeSteps 采取措施.
     * @apiParam {Number} storageId 所属库房id.
     * @apiError (Error 400) message
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void saveMonitoringRecord(@RequestBody MonitoringRecord monitoringRecord) {
        monitoringRecordService.save(monitoringRecord);
    }

    /**
     * @api {put} /monitoringRecord 修改监测记录
     * @apiName update
     * @apiGroup monitoringRecord
     * @apiParam {Number} id 监测记录ID
     * @apiParam {Number} monitoringPointId 监测点ID.
     * @apiParam {Timestamp} monitoringTime 监测时间.
     * @apiParam {Number} temperatureValue 温度值.(最大100，最小-100,如果该监测点类型是温度记录仪，则温度值不能为空且不显示湿度值；如果类型是温湿度记录仪则不能为空且温湿度都显示)
     * @apiParam {Number} humidityValue 湿度值.(最大100，最小-100，如果该监测点类型是湿度记录仪，则湿度值不能为空且不显示温度值；如果类型是温湿度记录仪则不能为空且温湿度都显示)
     * @apiParam {String{100}} takeSteps 采取措施.
     * @apiParam {Number} storageId 所属库房id.
     * @apiError (Error 400) message
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void updateMonitoringRecord(@RequestBody MonitoringRecord monitoringRecord) {
        monitoringRecordService.update(monitoringRecord);
    }

    /**
     * @api {delete} /monitoringRecord/deleteMonitoringRecordIds  删除监测记录
     * @apiName deleteMonitoringRecordIds
     * @apiGroup monitoringRecord
     * @apiParam {Array} monitoringRecordIds 监测记录ID数组
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "/deleteMonitoringRecordIds", method = RequestMethod.DELETE)
    public void delete(@JsonParam List<Integer> monitoringRecordIds) {
        monitoringRecordService.delete(monitoringRecordIds);
    }




}
