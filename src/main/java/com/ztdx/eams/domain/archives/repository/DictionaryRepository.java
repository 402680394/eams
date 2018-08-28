package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Dictionary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

/**
 * Created by li on 2018/4/18.
 */
@Repository
@Table(name = "business_dictionary")
@Qualifier("dictionaryRepository")
public interface DictionaryRepository extends JpaRepository<Dictionary, Integer> {

    Dictionary findById(int id);

    void deleteByClassificationId(int classificationId);

    boolean existsByCode(String code);

    boolean existsByCodeAndId(String code, int id);

    List<Dictionary> findByClassificationId(int classificationId);

    //查询分类排序优先级最大值
    @Query("select max (d.orderNumber) from Dictionary d where d.classificationId=:classificationId")
    Integer findMaxOrderNumber(@Param(value = "classificationId") int classificationId);

    //通过ID修改信息
    @Modifying
    @Query("update Dictionary d set d.code=:#{#dictionary.code},d.name=:#{#dictionary.name},d.businessLevel=:#{#dictionary.businessLevel},d.businessExpansion=:#{#dictionary.businessExpansion},d.remark=:#{#dictionary.remark} where d.id=:#{#dictionary.id}")
    void updateById(@Param(value = "dictionary") Dictionary dictionary);

    //修改排序号
    @Modifying
    @Query("update Dictionary d set d.orderNumber=:orderNumber where d.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);

    @Query("select d.name from Dictionary d ,DictionaryClassification dc where d.classificationId=dc.id and dc.code=:classificationCode")
    List<String> findByClassificationCode(@Param(value = "classificationCode") String classificationCode);

}
