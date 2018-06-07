package com.ztdx.eams.controller;

import com.ztdx.eams.basic.WorkContext;
import com.ztdx.eams.controller.operationLog.LogInfo;
import com.ztdx.eams.domain.system.application.OperationLogService;
import com.ztdx.eams.domain.system.model.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping(value = "/home")
public class HomeController {

    final OperationLogService operationLogService;

    @Autowired
    public HomeController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @RequestMapping(value = "/index/{id}")
    @LogInfo(message = "'操作了A' + args[0] +'返回值为' + result")
    public Integer get(@PathVariable("id") int id) {


        OperationLog operationLog = operationLogService.get("b0f051e1-faa5-448e-9f3e-b453274a3886");

        Random random = new Random();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(random.nextInt());
        }


        Integer max = list.stream().max(Comparator.comparing(u -> u)).get();
        return max;
    }
}
