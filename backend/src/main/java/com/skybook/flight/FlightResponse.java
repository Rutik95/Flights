package com.skybook.flight;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.skybook.airport.AirportResponse;

/**
 * A flight in search results. Times are in each airport's local time with the UTC offset
 * included (e.g. 2026-10-15T06:00:00+05:30), so the client can display them as-is.
 */
public record FlightResponse(
		Long id,
		String flightNumber,
		String airline,
		AirportResponse origin,
		AirportResponse destination,
		OffsetDateTime departureTime,
		OffsetDateTime arrivalTime,
		long durationMinutes,
		BigDecimal baseFare,
		int seatsAvailable) {

	public static FlightResponse from(Flight flight) {
		return new FlightResponse(
				flight.getId(),
				flight.getFlightNumber(),
				flight.getAirline(),
				AirportResponse.from(flight.getOrigin()),
				AirportResponse.from(flight.getDestination()),
				flight.getDepartureTime().atZone(flight.getOrigin().getZone()).toOffsetDateTime(),
				flight.getArrivalTime().atZone(flight.getDestination().getZone()).toOffsetDateTime(),
				flight.getDuration().toMinutes(),
				flight.getBaseFare(),
				flight.getAvailableSeats());
	}
}
