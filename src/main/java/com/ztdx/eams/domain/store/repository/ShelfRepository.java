package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Integer> {

    boolean existsByStorageIdAndName(int storageId, String name);

    boolean existsByStorageIdAndCode(int storageId, String code);

    boolean existsByStorageIdAndIdNotAndName(int storageId, int shelfId, String name);

    boolean existsByStorageIdAndIdNotAndCode(int storageId, int shelfId, String code);

    List<Shelf> findByStorageId(int storageId);
}
