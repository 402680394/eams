package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.Fonds;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/11.
 */
@Repository
@Table(name = "sys_fonds")
@Qualifier("fondsRepository")
public interface FondsRepository extends JpaRepository<Fonds, Integer> {
}
