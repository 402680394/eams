package com.ztdx.eams.controller.archives;

import com.ztdx.eams.query.ArchivesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by li on 2018/6/26.
 */
@RestController
@RequestMapping(value = "catalogue")
public class CatalogueController {

    private final ArchivesQuery archivesQuery;

    @Autowired
    public CatalogueController(ArchivesQuery archivesQuery) {
        this.archivesQuery = archivesQuery;
    }

}
