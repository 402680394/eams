package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.Shelf;
import com.ztdx.eams.domain.store.model.ShelfCell;
import com.ztdx.eams.domain.store.model.ShelfSection;
import com.ztdx.eams.domain.store.model.Storage;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomShelfCellRepositoryImpl implements CustomShelfCellRepository {

    private final EntityManager em;

    public CustomShelfCellRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    //@Transactional
    @Override
    public List<ShelfCell> createCell(ShelfSection section) {
        List<ShelfCell> list = new ArrayList<>();
        if (section.getSectionColNum() < 1 || section.getSectionRowNum() < 1){
            return list;
        }

        Shelf shelf = em.find(Shelf.class, section.getShelfId());
        Storage storage = em.find(Storage.class, section.getStorageId());
        String pointCodePrefix = String.format("%s-%s-%s", storage.getNumber(), shelf.getCode(), section.getCode());

        int side = 2;

        for (int o = 1; o <= side; o ++) {
            for (int i = 0; i < section.getSectionRowNum(); i++) {
                for (int j = 0; j < section.getSectionColNum(); j++) {
                    ShelfCell cell = new ShelfCell();
                    cell.setShelfSectionId(section.getId());
                    cell.setShelfId(section.getShelfId());
                    cell.setStorageId(section.getStorageId());
                    cell.setFondsId(section.getFondsId());
                    String sideStr;
                    if (o == 1) {
                        sideStr = "左";
                    }else{
                        sideStr = "右";
                    }
                    cell.setCode(String.format("%s-%d-%d", sideStr, i + 1, j + 1));
                    cell.setPointCode(String.format("%s-%s", pointCodePrefix, cell.getCode()));
                    cell.setSide(o);
                    cell.setColumnNo(j + 1);
                    cell.setRowNo(i + 1);
                    cell.setSectionCellLength(section.getSectionCellLength());
                    cell.setGmtCreate(Date.from(Instant.now()));
                    cell.setGmtDeleted(0);
                    list.add(cell);
                }
            }
        }

        List<ShelfCell> result = new ArrayList<>();

        for (ShelfCell entity : list) {
            em.persist(entity);
            result.add(entity);
        }
        return result;
    }
}
