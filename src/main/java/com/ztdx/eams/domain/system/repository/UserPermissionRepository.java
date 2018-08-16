package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    boolean existsByUserIdAndResourceUrlAndExpiryTimeGreaterThanEqual(int userId, String resourceUrl, Date now);

    List<UserPermission> findByUserIdAndExpiryTimeGreaterThanEqual(int userId, Date now);
}
