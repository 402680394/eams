package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.ShelfSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ShelfSectionRepository extends JpaRepository<ShelfSection, Integer>, CustomShelfSectionRepository {
    boolean existsByShelfIdAndNameAndGmtDeleted(int shelfId, String name, int gmtDeleted);

    boolean existsByShelfIdAndCodeAndGmtDeleted(int shelfId, String code, int gmtDeleted);

    int countByShelfIdAndGmtDeleted(int shelfId, int gmtDeleted);

    List<ShelfSection> findByShelfIdInAndGmtDeleted(Collection<Integer> ids, int gmtDeleted);
}
