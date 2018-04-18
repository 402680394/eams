package com.ztdx.eams.domain.business.repository;

import com.ztdx.eams.domain.business.model.DictionaryClassification;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/18.
 */
@Repository
@Table(name = "business_dictionary_classification")
@Qualifier("dictionaryClassificationRepository")
public interface DictionaryClassificationRepository extends JpaRepository<DictionaryClassification, Integer>{
}
