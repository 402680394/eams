package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByRoleNameAndFondsId(String roleName, long resourceId );

    /**
     * 某个节点上是否存在角色名称，自己的角色名称不算重复
     * @param roleName String 角色名称
     * @param resourceId long 资源节点id
     * @param roleId long 自己的角色id
     * @return boolean
     */
    boolean existsByRoleNameAndFondsIdAndIdNot(String roleName, long resourceId, long roleId );

    @Modifying
    @Query(value = "update Role set roleName = :#{#role.roleName} where id = :#{#role.id}")
    void updateById(Role role);

    List<Role> findByFondsIdIn(Iterable<Integer> fondsId);

    List<Role> findByFondsIdIsNull();

    List<Role> findByIdIn(Iterable<Long> ids);
}
