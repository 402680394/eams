package com.ztdx.eams.domain.business.repository;

import com.ztdx.eams.domain.business.model.Metadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/22.
 */
@Repository
@Table(name = "business_metadata")
@Qualifier("metadataRepository")
public interface MetadataRepository extends JpaRepository<Metadata, Integer> {
    void deleteByMetadataStandardsId(int metadataStandardsId);
}
