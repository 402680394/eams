package com.ztdx.eams.controller.work;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.domain.system.application.OrganizationService;
import com.ztdx.eams.domain.system.application.PermissionService;
import com.ztdx.eams.domain.system.application.UserService;
import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.domain.work.model.Workflow;
import com.ztdx.eams.domain.work.model.WorkflowCompleteEvent;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.variable.api.persistence.entity.VariableInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.flowable.common.engine.api.delegate.event.FlowableEngineEventType.PROCESS_COMPLETED;

@RestController
@RequestMapping(value = "/backlog")
public class BacklogController {

    private RuntimeService runtimeService;

    private TaskService taskService;

    private UserService userService;

    private OrganizationService organizationService;

    private HistoryService historyService;

    private ApplicationContext applicationContext;

    public BacklogController(RuntimeService runtimeService, TaskService taskService, UserService userService, OrganizationService organizationService, HistoryService historyService, ApplicationContext applicationContext) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.userService = userService;
        this.organizationService = organizationService;
        this.historyService = historyService;
        this.applicationContext = applicationContext;
    }

    /**
     * @api {get} /backlog/todoList?page={page}&size={size} 我的待办列表
     * @apiName todoList
     * @apiGroup backlog
     * @apiParam {Number} [page] 页码(QueryString)
     * @apiParam {Number} [size] 页行数(QueryString)
     * @apiSuccess {String} id 待办对象id
     * @apiSuccess {String} taskId 任务id
     * @apiSuccess {String="borrow"} type 待办类型 borrow:借阅
     * @apiSuccess {String} title 标题
     * @apiSuccess {String} orderCode 业务单号
     * @apiSuccess {String} orderId 业务单id
     * @apiSuccess {String} applicant 申请人姓名
     * @apiSuccess {String} company 公司
     * @apiSuccess {String} department 部门
     * @apiSuccess {Number} createTime 申请时间
     * @apiSuccessExample {json} Response-Example:
     * {
     * "data":{
     * "content":[
     * {
     * "id": "xxxxx-xxxx-xxx",
     * "taskId": "xxxxx-xxxx-xxx",
     * "type": "borrow",
     * "title": "论持久战",
     * "orderCode": "xxxxx-xxxx-xxx",
     * "orderId": 1,
     * "applicant": "",
     * "company": "",
     * "department": "",
     * "createTime":1534214071
     * }
     * ],
     * "totalElements": 14
     * }
     * }
     */
    @RequestMapping(path = "/todoList", method = RequestMethod.GET)
    public Map<String, Object> todoList(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page
            , @RequestParam(value = "size", required = false, defaultValue = "20") int size
            , @SessionAttribute UserCredential LOGIN_USER) {
        Integer userId = LOGIN_USER.getUserId();

        long count = taskService.createTaskQuery().taskAssignee(userId.toString()).count();
        List<Task> list = taskService
                .createTaskQuery()
                .taskAssignee(userId.toString())
                .orderByTaskCreateTime()
                .desc()
                .listPage(page * size, size);

        Collection<Integer> ids = list.stream().map(
                a -> {
                    Map<String, VariableInstance> vars = taskService.getVariableInstances(a.getId());
                    return vars.get("applicantId") == null ? 0 : Integer.parseInt(vars.get("applicantId").getValue().toString());
                }).collect(Collectors.toSet());
        ids.remove(0);

        List<User> users = userService.findAllById(ids);

        Collection<Integer> organIds = users.stream().map(User::getOrganizationId).collect(Collectors.toList());

        Map<Integer, List<String>> companies = organizationService.listDepartmentAndCompany(organIds);

        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getId, a -> a));

        Map<String, Object> result = new HashMap<>();
        result.put("content", list.stream().map(a -> {
            Map<String, VariableInstance> vars = taskService.getVariableInstances(a.getId());

            Map<String, Object> item = new HashMap<>();
            if (vars.size() != 0) {
                item.put("taskId", a.getId());
                item.put("id", vars.get("id").getValue());
                item.put("type", vars.get("type").getValue());
                item.put("title", vars.get("title").getValue());
                item.put("orderCode", vars.get("orderCode").getValue());
                item.put("orderId", vars.get("orderId").getValue());

                String applicantId = null;
                if (vars.get("applicantId") != null) {
                    applicantId = vars.get("applicantId").getValue().toString();
                }

                String name = null;
                String department = null;
                String company = null;
                if (StringUtils.isNumeric(applicantId) && userMap.get(Integer.parseInt(applicantId)) != null) {
                    User user = userMap.get(Integer.parseInt(applicantId));
                    name = user.getName();
                    List<String> pair = companies.get(user.getOrganizationId());
                    if (pair != null) {
                        department = pair.get(0);
                        company = pair.get(1);
                    }
                }

                item.put("applicant", name);
                item.put("company", company);
                item.put("department", department);
                item.put("createTime", a.getCreateTime());

            }
            return item;
        }).collect(Collectors.toList()));
        result.put("totalElements", count);
        return result;
    }

    /**
     * @api {get} /backlog/applyList?page={page}&size={size} 我的申请列表
     * @apiName applyList
     * @apiGroup backlog
     * @apiParam {Number} [page] 页码(QueryString)
     * @apiParam {Number} [size] 页行数(QueryString)
     * @apiSuccess {String} id 待办对象id
     * @apiSuccess {String="borrow"} type 待办类型 borrow:借阅
     * @apiSuccess {String} title 标题
     * @apiSuccess {String} orderCode 业务单号
     * @apiSuccess {String} orderId 业务单id
     * @apiSuccess {Number} createTime 申请时间
     * @apiSuccess {Number} endTime 审批时间
     * @apiSuccess {String="pending", "refuse", "agree"} result 结果
     * @apiSuccessExample {json} Response-Example:
     * {
     * "data":{
     * "content":[
     * {
     * "id": "xxxxx-xxxx-xxx",
     * "type": "borrow",
     * "title": "论持久战",
     * "orderCode": "xxxxx-xxxx-xxx",
     * "orderId": 1,
     * "createTime":1534214071,
     * "endTime": null,
     * "result": "pending"
     * }
     * ],
     * "totalElements": 14
     * }
     * }
     */
    @RequestMapping(path = "/applyList", method = RequestMethod.GET)
    public Map<String, Object> applyList(@RequestParam(value = "page", required = false, defaultValue = "0") int page
            , @RequestParam(value = "size", required = false, defaultValue = "20") int size
            , @SessionAttribute UserCredential LOGIN_USER) {
        Integer userId = LOGIN_USER.getUserId();

        long count = historyService.createHistoricProcessInstanceQuery().variableValueEquals("applicantId", userId).count();
        List<HistoricProcessInstance> list = historyService
                .createHistoricProcessInstanceQuery()
                .variableValueEquals("applicantId", userId)
                .orderByProcessInstanceStartTime()
                .desc()
                .listPage(page * size, size);

        Map<String, Object> result = new HashMap<>();
        result.put("content", list.stream().map(a -> {
            Map<String, Object> vars = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(a.getId())
                    .list()
                    .stream()
                    .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, HistoricVariableInstance::getValue));

            Map<String, Object> item = new HashMap<>();

            item.put("id", vars.get("id"));
            item.put("type", vars.get("type"));
            item.put("title", vars.get("title"));
            item.put("orderCode", vars.get("orderCode"));
            item.put("orderId", vars.get("orderId"));

            String resultStr = "pending";
            if (vars.get("status") != null) {
                resultStr = vars.get("status").toString();
            }
            item.put("createTime", a.getStartTime());
            item.put("endTime", a.getEndTime());
            item.put("result", resultStr);

            return item;
        }).collect(Collectors.toList()));
        result.put("totalElements", count);
        return result;
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
     * "type": "borrow",
     * "title": "论持久战",
     * "orderNo": "xxxxx-xxxx-xxx",
     * "applicant": {
     * "name": "",
     * "company": "",
     * "department": ""
     * },
     * "createTime":1534214071,
     * "endTime": 1534214071,
     * "result": "agree"
     * }
     */
    public void list() {

    }
}
