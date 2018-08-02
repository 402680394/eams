package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentTypeRepository extends JpaRepository<ContentType, Integer> {
}
