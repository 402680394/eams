package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.archivalCodeRuler.ArchivalCodeRuler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivalCodeRulerRepository extends JpaRepository<ArchivalCodeRuler, Integer> {

    List<ArchivalCodeRuler> findByCatalogueIdOrderByOrderNumber(int catalogueId);

}
