package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.BoxCodeRule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/7/13.
 */
@Repository
@Table(name = "store_box_code_rule")
@Qualifier("boxRepository")
public interface BoxCodeRuleRepository extends JpaRepository<BoxCodeRule, Integer> {

    BoxCodeRule findByArchivesIdAndType(int archivesId, byte type);
}
