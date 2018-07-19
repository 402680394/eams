package com.ztdx.eams.domain.archives.model.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Created by li on 2018/7/18.
 */
@Getter
public class EntryUnboxEvent extends ApplicationEvent {

    private int catalogueId;
    private List<String> boxCodes;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public EntryUnboxEvent(Object source, int catalogueId, List<String> boxCodes) {
        super(source);
        this.catalogueId = catalogueId;
        this.boxCodes = boxCodes;
    }
}
