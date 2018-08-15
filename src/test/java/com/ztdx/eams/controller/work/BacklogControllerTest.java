package com.ztdx.eams.controller.work;

import com.ztdx.eams.basic.UserCredential;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BacklogControllerTest {

    @Autowired
    private BacklogController backlogController;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Test
    public void todoList(){
        clear();

        Map<String, Object> map = new HashMap<>();
        map.put("id", "c837ce6a-2081-435a-8fd7-7ca9446aab80");
        map.put("type", "borrow");
        map.put("title", "测试借阅申请");
        map.put("orderCode", "JYD201808150001");
        map.put("orderId", 1);
        map.put("applicantId", 23);
        //ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("test", map);
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("test").variables(map).start();
        Map<String, Object> result = backlogController.todoList(0, 20, new UserCredential(22, "ceshi"));
    }

    @Test
    public void applyList(){
        Map<String, Object> result = backlogController.applyList(0, 20, new UserCredential(23, "ceshi"));
    }

    private void clear(){
        List<ProcessInstance> r = runtimeService.createProcessInstanceQuery().list();
        r.forEach(a -> {
            runtimeService.deleteProcessInstance(a.getId(), "clear");
        });
        historyService.createHistoricProcessInstanceQuery().list().forEach(a -> {
            historyService.deleteHistoricProcessInstance(a.getId());
        });
    }

    @Test
    public void start(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("test");
    }

    @Test
    public void agree() {
        List<ProcessInstance> r = runtimeService.createProcessInstanceQuery().list();

        r.forEach(a -> {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(a.getId()).list();
            tasks.forEach(b -> {
                Map<String, Object> map = new HashMap<>();
                map.put("result", "agree");
                taskService.complete(b.getId(), map);
            });
        });
    }

    @Test
    public void refuse() {
        List<ProcessInstance> r = runtimeService.createProcessInstanceQuery().list();

        r.forEach(a -> {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(a.getId()).list();
            tasks.forEach(b -> {
                Map<String, Object> map = new HashMap<>();
                map.put("result", "refuse");
                taskService.complete(b.getId(), map);
            });
        });
    }

    @Test
    public void delete(){
        List<ProcessInstance> r = runtimeService.createProcessInstanceQuery().list();

        r.forEach(a -> {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(a.getId()).list();
            tasks.forEach(b -> {
                taskService.deleteTask(b.getId());
            });
        });
    }

    @Test
    public void resolve(){
        List<ProcessInstance> r = runtimeService.createProcessInstanceQuery().list();

        r.forEach(a -> {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(a.getId()).list();
            tasks.forEach(b -> {
                taskService.resolveTask(b.getId());
            });
        });
    }

    @Test
    public void history(){
        List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery().list();
    }
}