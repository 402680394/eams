package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByRoleIdAndResourceUrlIn(long roleId, Collection<String> permissions);

    List<Permission> findByRoleIdAndResourceUrlNotIn(long roleId, Collection<String> permissions);

    List<Permission> findByRoleIdIn(Iterable<Long> roleIds);

    List<Permission> findByRoleId(long roleId);
}
