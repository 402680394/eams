package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.ArchivesService;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by li on 2018/5/3.
 */
@RestController
@RequestMapping(value = "/archivesGroup")
public class ArchivesGroupController {

    private final ArchivesQuery archivesQuery;

    private final ArchivesService archivesService;
    @Autowired
    public ArchivesGroupController(ArchivesQuery archivesQuery, ArchivesService archivesService) {
        this.archivesQuery = archivesQuery;
        this.archivesService = archivesService;
    }


}
