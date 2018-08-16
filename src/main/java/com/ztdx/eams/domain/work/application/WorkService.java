package com.ztdx.eams.domain.work.application;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WorkService {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    @Autowired
    public WorkService(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    //开启一个借阅流程
    public void startBorrow(Map<String, Object> map) {
        runtimeService.startProcessInstanceByKey("borrow", map);
    }
}
