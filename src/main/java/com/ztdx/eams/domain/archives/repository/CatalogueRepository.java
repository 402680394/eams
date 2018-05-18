package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Catalogue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogueRepository extends JpaRepository<Catalogue, Integer> {
}
