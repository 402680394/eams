package com.ztdx.eams.domain.business.repository;

import com.ztdx.eams.domain.business.model.Dictionary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/18.
 */
@Repository
@Table(name = "business_dictionary")
@Qualifier("dictionaryRepository")
public interface DictionaryRepository extends JpaRepository<Dictionary, Integer>{
}
