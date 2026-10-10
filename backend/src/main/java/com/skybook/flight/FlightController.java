package com.skybook.flight;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

	private final FlightService flightService;

	public FlightController(FlightService flightService) {
		this.flightService = flightService;
	}

	/**
	 * Example: GET /api/flights/search?origin=BOM&destination=DEL&date=2026-10-15&passengers=2&sort=PRICE
	 * Add returnDate=2026-10-20 for a round trip.
	 */
	@GetMapping("/search")
	public FlightSearchResponse search(@ModelAttribute FlightSearchRequest request) {
		return flightService.search(request);
	}
}
