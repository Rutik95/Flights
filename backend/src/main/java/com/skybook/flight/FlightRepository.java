package com.skybook.flight;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlightRepository extends JpaRepository<Flight, Long> {

	// JPQL uses entity/field names (Flight, origin.code), not table/column names.
	// Half-open window [from, to): a flight at exactly midnight belongs to one day only.
	@Query("""
			select f from Flight f
			where f.origin.code = :origin
			  and f.destination.code = :destination
			  and f.departureTime >= :from
			  and f.departureTime < :to
			  and f.availableSeats >= :passengers
			""")
	List<Flight> search(@Param("origin") String origin,
			@Param("destination") String destination,
			@Param("from") Instant from,
			@Param("to") Instant to,
			@Param("passengers") int passengers);
}
