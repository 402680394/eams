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

    /**
     * 通过所属机构id查询第一个user
     */
    User findFirstByOrganizationId(int id);

    /**
     * 通过id修改密码
     */
    @Modifying
    @Query("update User u set u.password=:password where u.id=:id")
    void updatePwdById(@Param("id") int id,@Param("password") String password);

    /**
     * 通过id修改状态
     */
    @Modifying
    @Query("update User u set u.flag=:flag where u.id=:id")
    void updateFlagById(@Param("id") int id,@Param("flag") int flag);
}
