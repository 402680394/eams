package com.ztdx.eams.domain.store.model.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

@Getter
public class BoxInsideEvent extends ApplicationEvent {

    private int catalogueId;
    private String boxCode;
    private Collection<String> ids;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source      the object on which the event initially occurred (never {@code null})
     * @param catalogueId 目录id
     * @param boxCode   盒号
     * @param ids    条目集合
     */
    public BoxInsideEvent(Object source, int catalogueId, String boxCode, Collection<String> ids) {
        super(source);
        this.catalogueId = catalogueId;
        this.boxCode = boxCode;
        this.ids = ids;
    }
}
