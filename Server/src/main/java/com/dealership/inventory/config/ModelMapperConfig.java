package com.dealership.inventory.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a single, shared {@link ModelMapper} bean for entity &lt;-&gt;
 * DTO conversions.
 * <p>
 * Note on Java {@code record} DTOs: ModelMapper can only <b>construct</b>
 * plain mutable classes (no-arg constructor + setters) as a mapping
 * destination - it cannot build a record, since records have no setters
 * and are only instantiable via their canonical constructor. Records work
 * fine as a mapping <b>source</b>, though, once field matching and private
 * field access are enabled (as below), since a record exposes its data as
 * private final fields rather than {@code getXxx()} methods. In practice
 * this means request records like {@code RegisterRequest} can be mapped
 * into mutable entities, while response records like {@code RegisterResponse}
 * still need a manual factory method (see {@code RegisterResponse.from}).
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STANDARD)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(AccessLevel.PRIVATE);
        return modelMapper;
    }
}
