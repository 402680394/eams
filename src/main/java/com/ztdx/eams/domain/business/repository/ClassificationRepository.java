package com.ztdx.eams.domain.business.repository;

import com.ztdx.eams.domain.business.model.Classification;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/18.
 */
@Repository
@Table(name = "business_classification")
@Qualifier("classificationRepository")
public interface ClassificationRepository extends JpaRepository<Classification, Integer>{
}
