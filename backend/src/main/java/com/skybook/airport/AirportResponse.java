package com.skybook.airport;

/**
 * API representation of an airport. Deliberately excludes internal fields (id, zoneId).
 */
public record AirportResponse(String code, String name, String city, String country) {

	public static AirportResponse from(Airport airport) {
		return new AirportResponse(airport.getCode(), airport.getName(), airport.getCity(), airport.getCountry());
	}
}
