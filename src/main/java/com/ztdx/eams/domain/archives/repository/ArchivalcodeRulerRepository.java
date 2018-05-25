package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.model.archivalcodeRuler.ArchivalcodeRuler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivalcodeRulerRepository extends JpaRepository<ArchivalcodeRuler, Integer> {

    List<ArchivalcodeRuler> findByCatalogueId(int catalogueId);

}
