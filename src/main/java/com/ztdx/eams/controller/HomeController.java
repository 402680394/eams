package com.ztdx.eams.controller;

import com.ztdx.eams.basic.log.OperationLog;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/home")
public class HomeController {

    @RequestMapping(value = "/index/{id}")
    @OperationLog(key = "#args[0]")
    public void get(@PathVariable("id") int id){

    }
}
