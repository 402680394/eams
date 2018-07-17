package com.ztdx.eams.domain.store.model.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

/**
 * 密集架格删除事件
 */
@Getter
public class ShelfCellDeletedEvent extends ApplicationEvent {

    /**
     * 删除的密集架格的库位码
     */
    private final Collection<String> shelfCellPointCodes;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ShelfCellDeletedEvent(Object source, Collection<String> shelfCellPointCodes) {
        super(source);
        this.shelfCellPointCodes = shelfCellPointCodes;
    }
}
