package com.skybook.airport;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {

	// Derived query: Spring Data parses the method name into "... WHERE code = ?"
	Optional<Airport> findByCode(String code);
}
