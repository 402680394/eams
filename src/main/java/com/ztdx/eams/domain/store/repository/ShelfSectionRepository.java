package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.ShelfSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ShelfSectionRepository extends JpaRepository<ShelfSection, Integer>, CustomShelfSectionRepository {
    boolean existsByShelfIdAndName(int shelfId, String name);

    boolean existsByShelfIdAndCode(int shelfId, String code);

    int countByShelfIdAndGmtDeleted(int shelfId, int gmtDeleted);

    List<ShelfSection> findByShelfIdIn(Collection<Integer> ids);
}
