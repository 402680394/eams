package com.ztdx.eams.domain.work.application;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
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
        System.out.println(processInstance.getId());
    }

    //同意
    @Transactional
    public void agree(List<String> processIds) {

        List<Task> list = taskService.createTaskQuery().processInstanceIdIn(processIds).list();
        Map<String, Object> map = new HashMap<>();
        map.put("approvalTime", new Date());
        map.put("result", "agree");
        list.forEach(a -> {
            System.out.println("执行任务id" + a.getId());
            System.out.println("执行任务名" + a.getName());
            //判断流程是否执行结束
            ProcessInstance p = runtimeService.createProcessInstanceQuery().processInstanceId(a.getProcessInstanceId()).singleResult();
            if (p.isEnded()) {
                System.out.println("流程已结束");
                map.put("status", "已结束");
            } else {
                System.out.println("流程已审批");
                map.put("status", "审批中");
            }
            taskService.complete(a.getId(), map);
        });
        //        processIds.forEach(processId -> {
//            Execution execution = runtimeService.createExecutionQuery().processInstanceId(processId).singleResult();
//            Map<String, Object> map = new HashMap<>();
//            map.put("approvalTime", new Date());
//            map.put("result", "agree");
//            System.out.println("执行流程：" + execution.getName());
//            if (execution.isEnded()) {
//                System.out.println("流程已结束");
//                map.put("status", "已结束");
//            } else {
//                System.out.println("流程已审批");
//                map.put("status", "审批中");
//            }
//            runtimeService.signalEventReceived(execution.getId());
//        });
    }

    //拒绝
    @Transactional
    public void refuse(List<String> processId) {
        List<Task> list = taskService.createTaskQuery().processInstanceIdIn(processId).list();
        Map<String, Object> map = new HashMap<>();
        map.put("approvalTime", new Date());
        map.put("status", "已拒绝");
        map.put("result", "refuse");
        list.forEach(a -> {
            taskService.complete(a.getId(), map);
        });
    }

    public Map<String, Object> queryByVariable(int orderId, int size, int page) {

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
            borrowContent.put("processId", historicProcessInstance.getId());
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

        HistoricVariableInstance historicVariableInstance = historicVariableInstances.get(0);

        resultMap.put("borrowContent", borrowContents);
        resultMap.put("total", total);

        return resultMap;
    }
}
