package com.ztdx.eams.domain.work.application;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.variable.api.persistence.entity.VariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WorkService {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final HistoryService historyService;

    @Autowired
    public WorkService(RuntimeService runtimeService, TaskService taskService, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
    }

    //开启一个流程
    public void start(String processKey, Map<String, Object> map) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, map);
    }

    //同意
    @Transactional
    public void agree(List<String> taskIds) {

        Map<String, Object> map = new HashMap<>();
        map.put("approvalTime", new Date());
        map.put("result", "agree");
        taskIds.forEach(a -> {
            taskService.complete(a, map);
        });
    }

    //拒绝
    @Transactional
    public void refuse(List<String> taskIds) {
        Map<String, Object> map = new HashMap<>();
        map.put("approvalTime", new Date());
        map.put("result", "refuse");
        taskIds.forEach(a -> {
            taskService.complete(a, map);
        });
    }

    public Map<String, Object> queryAllBorrowData(int orderId, int size, int page) {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> borrowContents = new ArrayList<>();

        long total = historyService.createHistoricProcessInstanceQuery().variableValueEquals("orderId", orderId).count();
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().variableValueEquals("orderId", orderId).listPage(page - 1, size);

        List<HistoricVariableInstance> historicVariableInstances = null;
        for (HistoricProcessInstance historicProcessInstance : list) {
            historicVariableInstances =
                    historyService.createHistoricVariableInstanceQuery()
                            .processInstanceId(historicProcessInstance.getId())
                            .list();
            Map<String, Object> borrowContent = new HashMap<>();
            historicVariableInstances.forEach(b -> {
                if (b.getVariableName().equals("archiveName")
                        || b.getVariableName().equals("title")
                        || b.getVariableName().equals("reference")
                        || b.getVariableName().equals("approvalTime")
                        || b.getVariableName().equals("status")
                        || b.getVariableName().equals("days"))
                    borrowContent.put(b.getVariableName(), b.getValue());
            });
            borrowContents.add(borrowContent);
        }

        resultMap.put("borrowContent", borrowContents);
        resultMap.put("total", total);

        return resultMap;
    }

    public Map<String, Object> queryApprovalBorrowData(int orderId, int size, int page) {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> borrowContents = new ArrayList<>();

        long total = taskService.createTaskQuery().processVariableValueEquals("orderId", orderId).count();
        List<Task> list = taskService.createTaskQuery().processVariableValueEquals("orderId", orderId).listPage(page - 1, size);

        for (Task task : list) {
            Map<String, Object> borrowContent = new HashMap<>();
            Map<String, VariableInstance> variableMap = taskService.getVariableInstances(task.getId());

            borrowContent.put("taskId", task.getId());
            borrowContent.put("archiveName", variableMap.get("archiveName").getValue());
            borrowContent.put("title", variableMap.get("title").getValue());
            borrowContent.put("reference", variableMap.get("reference").getValue());
            borrowContent.put("approvalTime", variableMap.get("approvalTime").getValue());
            borrowContent.put("status", variableMap.get("status").getValue());
            borrowContent.put("days", variableMap.get("days").getValue());

            borrowContents.add(borrowContent);
        }

        resultMap.put("borrowContent", borrowContents);
        resultMap.put("total", total);

        return resultMap;
    }
}
