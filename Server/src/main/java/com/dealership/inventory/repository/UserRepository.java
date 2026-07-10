package com.dealership.inventory.repository;

import com.dealership.inventory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link User} records.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
