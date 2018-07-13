package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.ShelfCell;
import com.ztdx.eams.domain.store.model.ShelfSection;

import java.util.List;

public interface CustomShelfCellRepository {
    List<ShelfCell> createCell(ShelfSection section);
}
