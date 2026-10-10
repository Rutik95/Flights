package com.skybook.flight;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Query parameters of GET /api/flights/search, bound by Spring MVC into one object.
 * A missing returnDate means one-way. Bean Validation annotations are added in Step 2.
 */
public record FlightSearchRequest(
		String origin,
		String destination,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
		Integer passengers,
		FlightSort sort) {

	// Compact constructor: applies defaults for optional parameters
	public FlightSearchRequest {
		if (passengers == null) {
			passengers = 1;
		}
		if (sort == null) {
			sort = FlightSort.PRICE;
		}
	}

	public boolean isRoundTrip() {
		return returnDate != null;
	}
}
