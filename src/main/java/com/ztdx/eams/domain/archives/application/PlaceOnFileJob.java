package com.ztdx.eams.domain.archives.application;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by li on 2018/6/15.
 */
@Service
public class PlaceOnFileJob extends QuartzJobBean {

    private OriginalTextService originalTextService;

    public OriginalTextService getOriginalTextService() {
        return originalTextService;
    }
    @Autowired
    public void setOriginalTextService(OriginalTextService originalTextService) {
        this.originalTextService = originalTextService;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        int catalogueId = jobExecutionContext.getJobDetail().getJobDataMap().getInt("catalogueId");
        List<String> ids = (List<String>) jobExecutionContext.getJobDetail().getJobDataMap().get("ids");
        for (String id : ids) {
            originalTextService.placeOnFile(id, catalogueId);
        }
    }
}
