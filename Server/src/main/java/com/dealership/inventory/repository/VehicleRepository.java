package com.dealership.inventory.repository;

import com.dealership.inventory.entity.Vehicle;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

	boolean existsByModel(String model);

	boolean existsByModelAndIdNot(String model, Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select v from Vehicle v where v.id = :id")
	Optional<Vehicle> findByIdForUpdate(@Param("id") Long id);
}