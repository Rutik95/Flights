package com.skybook.flight;

import java.util.Comparator;

/**
 * Sort options for search results. Sorted in Java rather than SQL because duration is
 * derived (not a column) and a single route/day returns only a handful of flights.
 */
public enum FlightSort {

	PRICE(Comparator.comparing(Flight::getBaseFare)),
	DEPARTURE(Comparator.comparing(Flight::getDepartureTime)),
	DURATION(Comparator.comparing(Flight::getDuration));

	private final Comparator<Flight> comparator;

	FlightSort(Comparator<Flight> comparator) {
		// Tie-breaker keeps the order stable and predictable (e.g. two flights with the same price)
		this.comparator = comparator.thenComparing(Flight::getDepartureTime);
	}

	public Comparator<Flight> comparator() {
		return comparator;
	}
}
