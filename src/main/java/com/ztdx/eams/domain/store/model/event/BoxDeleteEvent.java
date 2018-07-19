package com.ztdx.eams.domain.store.model.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Created by li on 2018/7/18.
 */
@Getter
public class BoxDeleteEvent extends ApplicationEvent {

    private int catalogueId;
    private List<Integer> boxIds;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public BoxDeleteEvent(Object source, int catalogueId, List<Integer> boxIds) {
        super(source);
        this.catalogueId = catalogueId;
        this.boxIds = boxIds;
    }
}
