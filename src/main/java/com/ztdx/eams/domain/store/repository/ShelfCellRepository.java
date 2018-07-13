package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.ShelfCell;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShelfCellRepository extends JpaRepository<ShelfCell, Integer>, CustomShelfCellRepository {
    boolean existsByBarCodeAndIdNot(String barCode, int id);

    boolean existsByPointCodeAndIdNot(String pointCode, int id);

    Page<ShelfCell> findByShelfSectionId(int shelfSectionId, Pageable pageable);
}
