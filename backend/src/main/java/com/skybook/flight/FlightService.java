package com.skybook.flight;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skybook.airport.Airport;
import com.skybook.airport.AirportRepository;
import com.skybook.common.InvalidRequestException;

@Service
public class FlightService {

	private final FlightRepository flightRepository;
	private final AirportRepository airportRepository;

	public FlightService(FlightRepository flightRepository, AirportRepository airportRepository) {
		this.flightRepository = flightRepository;
		this.airportRepository = airportRepository;
	}

	// Entities are mapped to DTOs inside the transaction: with open-in-view=false,
	// touching a lazy association after this method returns would throw LazyInitializationException.
	// Input shape (required fields, ranges, return >= depart) is already validated by Bean Validation
	// in the controller; the service checks what only it can know, like whether an airport exists.
	@Transactional(readOnly = true)
	public FlightSearchResponse search(FlightSearchRequest request) {
		Airport origin = findAirport(request.origin());
		Airport destination = findAirport(request.destination());

		List<FlightResponse> outbound = findFlights(origin, destination, request.date(), request);
		List<FlightResponse> inbound = request.isRoundTrip()
				? findFlights(destination, origin, request.returnDate(), request)
				: List.of();

		return new FlightSearchResponse(outbound, inbound);
	}

	private List<FlightResponse> findFlights(Airport from, Airport to, LocalDate date, FlightSearchRequest request) {
		// "15 Oct" means 15 Oct in the departure airport's time zone.
		// atStartOfDay(zone) is DST-safe (midnight may not exist on DST-change days in some zones).
		ZoneId zone = from.getZone();
		Instant start = date.atStartOfDay(zone).toInstant();
		Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();

		return flightRepository.search(from.getCode(), to.getCode(), start, end, request.passengers()).stream()
				.sorted(request.sort().comparator())
				.map(FlightResponse::from)
				.toList();
	}

	private Airport findAirport(String code) {
		return airportRepository.findByCode(code)
				.orElseThrow(() -> new InvalidRequestException("Unknown airport: " + code));
	}
}
