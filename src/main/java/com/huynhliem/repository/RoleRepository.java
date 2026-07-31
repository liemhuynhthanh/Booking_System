package com.huynhliem.repository;

import com.huynhliem.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    @Query("select r from Role r join fetch r.users u where u.id = :userId")
    Role findRoleByUserId(@Param("userId") Long userId);

    Optional<Role> findByRoleName(String roleName);
}
