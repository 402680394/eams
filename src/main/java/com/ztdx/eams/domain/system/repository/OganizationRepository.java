package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.Oganization;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/11.
 */
@Repository
@Table(name = "sys_organization")
@Qualifier("organizationRepository")
public interface OganizationRepository extends JpaRepository<Oganization, Integer> {

}
