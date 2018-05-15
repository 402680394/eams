package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.DescriptionItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/5/15.
 */
@Repository
@Table(name = "archives_description_item")
@Qualifier("descriptionItemRepository")
public interface DescriptionItemRepository extends JpaRepository<DescriptionItem, Integer> {
}
