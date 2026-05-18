package com.mes.repository;

import com.mes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmployeeCode(String employeeCode);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(u.employeeCode) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    List<User> searchUsers(String keyword);

    List<User> findByRoleName(String roleName);
}
