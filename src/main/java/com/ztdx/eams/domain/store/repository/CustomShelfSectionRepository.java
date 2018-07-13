package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.Shelf;
import com.ztdx.eams.domain.store.model.ShelfSection;

import java.util.List;

public interface CustomShelfSectionRepository {
    List<ShelfSection> createSection(Shelf shelf);
}
