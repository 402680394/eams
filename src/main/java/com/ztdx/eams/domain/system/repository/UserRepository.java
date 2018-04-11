package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "sys_user")
@Qualifier("userRepository")
public interface UserRepository
        extends JpaRepository<User, Integer> {
    /**
    * 通过用户名查询user
    */
    User findByUsername(String username);

}
