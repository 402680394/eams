package com.ztdx.eams.domain.business.repository;

import com.ztdx.eams.domain.business.model.DictionaryClassification;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/18.
 */
@Repository
@Table(name = "business_dictionary_classification")
@Qualifier("dictionaryClassificationRepository")
public interface DictionaryClassificationRepository extends JpaRepository<DictionaryClassification, Integer> {

    //查询词典分类编码是否存在
    boolean existsByCode(String code);

    boolean existsByCodeAndId(String code, int id);

    //通过ID修改信息
    @Modifying
    @Query("update DictionaryClassification d set d.fondsId=:#{#dictionaryClassification.fondsId},d.code=:#{#dictionaryClassification.code},d.name=:#{#dictionaryClassification.name},d.remark=:#{#dictionaryClassification.remark} where d.id=:#{#dictionaryClassification.id}")
    void updateById(@Param(value = "dictionaryClassification")DictionaryClassification dictionaryClassification);
}
