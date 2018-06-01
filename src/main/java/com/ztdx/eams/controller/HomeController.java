package com.ztdx.eams.controller;

import com.ztdx.eams.controller.operationLog.LogInfo;
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

    @RequestMapping(value = "/index/{id}")
    @LogInfo(message = "'HELLO world' + args[0]")
    public void get(@PathVariable("id") int id){

        Random random =new Random();

        List<Integer> list =new ArrayList<>();
        for (int i=0;i<100;i++){
            list.add(random.nextInt());
        }

        Integer max = list.stream().max(Comparator.comparing(u->u)).get();

    }
}
