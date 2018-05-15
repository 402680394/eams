package com.ztdx.eams.controller;

//import com.ztdx.eams.domain.archives.model.ArchivesType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/home/")
public class HomeController {

    @RequestMapping(value = "")
    public Map get(){
        Map map =new HashMap();
//        for (ArchivesType item : ArchivesType.values()) {
//            map.put(item.getCode(), item.getDescription());
//        }



        return  map;

    }
}
