package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.ShelfCell;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShelfCellRepository extends JpaRepository<ShelfCell, Integer>, CustomShelfCellRepository {
    boolean existsByBarCodeAndIdNotAndGmtDeleted(String barCode, int id, int gmtDeleted);

    boolean existsByPointCodeAndIdNotAndGmtDeleted(String pointCode, int id, int gmtDeleted);

    Page<ShelfCell> findByShelfSectionIdAndGmtDeleted(int shelfSectionId, Pageable pageable, int gmtDeleted);

    List<ShelfCell> findByShelfIdIn(Collection<Integer> shelfIds);
}
