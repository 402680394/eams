package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.business.model.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivesRepository  extends JpaRepository<Archives, Integer> {

}
