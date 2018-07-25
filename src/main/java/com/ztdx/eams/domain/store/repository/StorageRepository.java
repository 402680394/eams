package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.archives.model.MetadataStandards;
import com.ztdx.eams.domain.store.model.Storage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

@Repository
@Table(name = "store_storage")
@Qualifier("storageRepository")
public interface StorageRepository extends JpaRepository<Storage, Integer> {

    boolean existsByNumberAndGmtDeleted(String number,int gmtDeleted);

    //通过ID修改
    @Modifying
    @Query("update Storage s set s.name=:#{#storage.name},s.number=:#{#storage.number},s.description=:#{#storage.description} where s.id=:#{#storage.id}")
    void updateById(@Param(value = "storage") Storage storage);

    List<Storage> findAllByFondsId(int fondsIds);
}
