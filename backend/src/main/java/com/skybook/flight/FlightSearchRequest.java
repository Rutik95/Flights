package com.skybook.flight;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Query parameters of GET /api/flights/search, bound by Spring MVC into one object and
 * validated with Bean Validation (@Valid in the controller). A missing returnDate means one-way.
 * Field annotations check each value; {@link ValidFlightSearch} checks rules across fields.
 */
@ValidFlightSearch
public record FlightSearchRequest(
		@NotBlank @Size(min = 3, max = 3, message = "must be a 3-letter airport code") String origin,
		@NotBlank @Size(min = 3, max = 3, message = "must be a 3-letter airport code") String destination,
		@NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
		@Min(1) @Max(9) Integer passengers,
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
