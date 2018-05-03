package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.ArchivesService;
import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.model.ArchivesType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/archives")
public class ArchivesController {

    private final ArchivesService archivesService;

    @Autowired
    public ArchivesController(ArchivesService archivesService) {
        this.archivesService = archivesService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Archives get() {
        Archives archives =  archivesService.get(1);
        archives.setArchivesType(ArchivesType.Project);
        return  archives;
//        Archives archives = new Archives();
//        archives.setName("档案库");
//        archives.setArchivesType(ArchivesType.Project);
//        archivesService.save(archives);
//        return archives;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public Archives post(@RequestBody Archives archives) {
        return  archives;
//        Archives archives = new Archives();
//        archives.setName("档案库");
//        archives.setArchivesType(ArchivesType.Project);
//        archivesService.save(archives);
//        return archives;
    }
}
