package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.Archives;
import com.ztdx.eams.query.jooq.tables.ArchivesCatalogue;
import com.ztdx.eams.query.jooq.tables.ArchivesGroup;
import com.ztdx.eams.query.jooq.tables.SysFonds;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by li on 2018/5/3.
 */
@Service
public class ArchivesQuery {

    private final DSLContext dslContext;

    private SysFonds sysFonds = Tables.SYS_FONDS;

    private ArchivesGroup archivesGroup=Tables.ARCHIVES_GROUP;

    private Archives archives=Tables.ARCHIVES;

    private ArchivesCatalogue archivesCatalogue=Tables.ARCHIVES_CATALOGUE;

    @Autowired
    public ArchivesQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
    }


}
