package com.ztdx.eams.controller.work;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/backlog")
public class BacklogController {

    /**
     * @api {get} /backlog/todoList?page={page}&size={size} 我的待办列表
     * @apiName todoList
     * @apiGroup backlog
     * @apiParam {Number} [page] 页码(QueryString)
     * @apiParam {Number} [size] 页行数(QueryString)
     * @apiSuccess {String="borrow"} type 待办类型 borrow:借阅
     * @apiSuccess {String} title 标题
     * @apiSuccess {String} orderNo 单号
     * @apiSuccess {Object} applicant 申请人
     * @apiSuccess {String} applicant.name 姓名
     * @apiSuccess {String} applicant.company 公司
     * @apiSuccess {String} applicant.department 部门
     * @apiSuccess {Number} createTime 申请时间
     * @apiSuccessExample {json} Response-Example:
     * {
     *     "type": "borrow",
     *     "title": "论持久战",
     *     "orderNo": "xxxxx-xxxx-xxx",
     *     "applicant": {
     *         "name": "",
     *         "company": "",
     *         "department": ""
     *     },
     *     "createTime":1534214071
     * }
     */
    public void todoList(){

    }

    /**
     * @api {get} /backlog/applyList?page={page}&size={size} 我的申请列表
     * @apiName applyList
     * @apiGroup backlog
     * @apiParam {Number} [page] 页码(QueryString)
     * @apiParam {Number} [size] 页行数(QueryString)
     * @apiSuccess {String="borrow"} type 待办类型 borrow:借阅
     * @apiSuccess {String} title 标题
     * @apiSuccess {String} orderNo 单号
     * @apiSuccess {Number} createTime 申请时间
     * @apiSuccess {Number} endTime 审批时间
     * @apiSuccess {String="pending", "refuse", "agree"} result 结果
     * @apiSuccessExample {json} Response-Example:
     * {
     *     "type": "borrow",
     *     "title": "论持久战",
     *     "orderNo": "xxxxx-xxxx-xxx",
     *     "createTime":1534214071,
     *     "endTime": null,
     *     "result": "pending",
     * }
     */
    public void applyList(){

    }

    /**
     * @api {get} /backlog/list?page={page}&size={size} 工作列表
     * @apiName list
     * @apiGroup backlog
     * @apiSuccess {String="borrow"} type 待办类型 borrow:借阅
     * @apiSuccess {String} title 标题
     * @apiSuccess {String} orderNo 单号
     * @apiSuccess {Object} applicant 申请人
     * @apiSuccess {String} applicant.name 姓名
     * @apiSuccess {String} applicant.company 公司
     * @apiSuccess {String} applicant.department 部门
     * @apiSuccess {Number} createTime 申请时间
     * @apiSuccess {Number} endTime 审批时间
     * @apiSuccess {String="pending", "refuse", "agree"} result 结果
     * @apiSuccessExample {json} Response-Example:
     * {
     *     "type": "borrow",
     *     "title": "论持久战",
     *     "orderNo": "xxxxx-xxxx-xxx",
     *     "applicant": {
     *         "name": "",
     *         "company": "",
     *         "department": ""
     *     },
     *     "createTime":1534214071,
     *     "endTime": 1534214071,
     *     "result": "agree",
     * }
     */
    public void list(){

    }
}
