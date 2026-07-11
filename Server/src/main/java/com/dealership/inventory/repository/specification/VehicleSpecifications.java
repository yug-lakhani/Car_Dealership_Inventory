package com.dealership.inventory.repository.specification;

import com.dealership.inventory.entity.Vehicle;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Specification helpers for dynamic vehicle searches.
 */
public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> hasMake(String make) {
        return (root, query, criteriaBuilder) -> StringUtils.hasText(make)
                ? criteriaBuilder.equal(criteriaBuilder.lower(root.get("make")), make.trim().toLowerCase())
                : null;
    }

    public static Specification<Vehicle> hasModel(String model) {
        return (root, query, criteriaBuilder) -> StringUtils.hasText(model)
                ? criteriaBuilder.equal(criteriaBuilder.lower(root.get("model")), model.trim().toLowerCase())
                : null;
    }

    public static Specification<Vehicle> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> StringUtils.hasText(category)
                ? criteriaBuilder.equal(criteriaBuilder.lower(root.get("category")), category.trim().toLowerCase())
                : null;
    }

    public static Specification<Vehicle> priceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> minPrice != null
                ? criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice)
                : null;
    }

    public static Specification<Vehicle> priceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> maxPrice != null
                ? criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice)
                : null;
    }
}