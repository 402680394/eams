package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.Shelf;
import com.ztdx.eams.domain.store.model.ShelfSection;
import org.apache.commons.lang.StringUtils;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomShelfSectionRepositoryImpl implements CustomShelfSectionRepository {

    private final EntityManager em;

    protected CustomShelfSectionRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<ShelfSection> createSection(Shelf shelf) {
        List<ShelfSection> list = new ArrayList<>();

        if (shelf.getSectionNum() < 1){
            return list;
        }

        for (int i = 0; i < shelf.getSectionNum(); i++) {
            String codePrefix = StringUtils.isEmpty(shelf.getSectionCodePrefix()) ? "J-" : shelf.getSectionCodePrefix();
            String namePrefix = StringUtils.isEmpty(shelf.getSectionNamePrefix()) ? "密集架" : shelf.getSectionNamePrefix();
            int sn = i + 1;
            if (shelf.getSectionStartSn() != null){
                sn = shelf.getSectionStartSn() + i;
            }

            ShelfSection section = new ShelfSection();
            section.setShelfId(shelf.getId());
            section.setStorageId(shelf.getStorageId());
            section.setFondsId(shelf.getFondsId());
            section.setCode(codePrefix + sn );
            section.setLeftTag(section.getCode() + "-l");
            section.setRightTag(section.getCode() + "-r");
            section.setName(namePrefix + sn );
            section.setShelfSectionType(4);
            section.setSectionColNum(shelf.getSectionColNum());
            section.setSectionRowNum(shelf.getSectionRowNum());
            section.setSectionCellLength(shelf.getSectionCellLength());
            section.setSectionCellWidth(shelf.getSectionCellWidth());
            section.setSectionCellHeight(shelf.getSectionCellHeight());
            section.setGmtCreate(Date.from(Instant.now()));
            section.setGmtDeleted(0);
            list.add(section);
        }

        List<ShelfSection> result = new ArrayList<>();

        for (ShelfSection entity : list) {
            em.persist(entity);
            result.add(entity);
        }
        return result;
    }
}
