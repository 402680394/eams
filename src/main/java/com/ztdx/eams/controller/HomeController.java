package com.ztdx.eams.controller;

import com.ztdx.eams.controller.operationLog.OperationInfo;
import com.ztdx.eams.domain.system.application.OperationRecordService;
import com.ztdx.eams.domain.system.model.OperationRecord;
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

    private final OperationRecordService operationRecordService;

    @Autowired
    public HomeController(OperationRecordService operationRecordService) {
        this.operationRecordService = operationRecordService;
    }

    @RequestMapping(value = "/index/{id}")
    @OperationInfo(message = "'操作了A' + args[0] +'返回值为' + result")
    public Integer get(@PathVariable("id") int id) {


        OperationRecord operationRecord = operationRecordService.get("b0f051e1-faa5-448e-9f3e-b453274a3886");

        Random random = new Random();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(random.nextInt());
        }


        return list.stream().max(Comparator.comparing(u -> u)).get();
    }
}
