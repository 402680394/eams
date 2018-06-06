package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.RoleOfUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoleOfUserRepository extends JpaRepository<RoleOfUser, Long> {

    List<RoleOfUser> findByRoleIdAndUserIdIn(long roleId, Collection<Integer> userIds);

    List<RoleOfUser> findByUserId(int userId);

    List<RoleOfUser> findByRoleId(long roleId);
}
