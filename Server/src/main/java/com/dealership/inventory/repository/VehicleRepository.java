package com.dealership.inventory.repository;

import com.dealership.inventory.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

	boolean existsByModel(String model);

	boolean existsByModelAndIdNot(String model, Long id);
}