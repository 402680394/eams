package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    List<UserPermission> findByUserIdAndResourceUrlInAndExpiryTimeGreaterThanEqual(int userId, Collection<String> resourceUrls, Date now);

    List<UserPermission> findByUserIdAndExpiryTimeGreaterThanEqual(int userId, Date now);
}
