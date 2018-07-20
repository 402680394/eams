package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Integer> {

    boolean existsByStorageIdAndNameAndGmtDeleted(int storageId, String name, int gmtDeleted);

    boolean existsByStorageIdAndCodeAndGmtDeleted(int storageId, String code, int gmtDeleted);

    boolean existsByStorageIdAndIdNotAndNameAndGmtDeleted(int storageId, int shelfId, String name, int gmtDeleted);

    boolean existsByStorageIdAndIdNotAndCodeAndGmtDeleted(int storageId, int shelfId, String code, int gmtDeleted);

    List<Shelf> findByStorageIdAndGmtDeleted(int storageId, int gmtDeleted);

    List<Shelf> findByStorageIdInAndGmtDeleted(Collection<Integer> storageIds, int gmtDeleted);
}
