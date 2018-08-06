package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.OriginalText;
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

    public List<OriginalText> getList() {
        return list;
    }

    public void setList(List<OriginalText> list) {
        this.list = list;
    }

    private List<OriginalText> list;


    public OriginalTextService getOriginalTextService() {
        return originalTextService;
    }

    @Autowired
    public void setOriginalTextService(OriginalTextService originalTextService) {
        this.originalTextService = originalTextService;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

//        for (OriginalText originalText : list) {
//            originalTextService.placeOnFile(originalText);
//        }
    }
}
