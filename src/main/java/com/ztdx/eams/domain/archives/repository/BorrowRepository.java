package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Borrow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "archives_borrow")
@Qualifier("borrowRepository")
public interface BorrowRepository extends JpaRepository<Borrow, Integer> {

}
