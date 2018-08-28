package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "archives_content_type")
@Qualifier("contentTypeRepository")
public interface ContentTypeRepository extends JpaRepository<ContentType, Integer> {
}
