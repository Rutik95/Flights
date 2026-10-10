package com.skybook.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skybook.airport.Airport;
import com.skybook.airport.AirportRepository;

/**
 * Loads sample data once on startup. Idempotent: skips seeding if data already exists,
 * so restarts don't create duplicates.
 */
@Component
public class DataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final AirportRepository airportRepository;

	public DataSeeder(AirportRepository airportRepository) {
		this.airportRepository = airportRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (airportRepository.count() > 0) {
			log.info("Seed data already present, skipping");
			return;
		}

		List<Airport> airports = airportRepository.saveAll(List.of(
				new Airport("BOM", "Chhatrapati Shivaji Maharaj Intl", "Mumbai", "India", "Asia/Kolkata"),
				new Airport("DEL", "Indira Gandhi Intl", "Delhi", "India", "Asia/Kolkata"),
				new Airport("BLR", "Kempegowda Intl", "Bengaluru", "India", "Asia/Kolkata"),
				new Airport("MAA", "Chennai Intl", "Chennai", "India", "Asia/Kolkata"),
				new Airport("HYD", "Rajiv Gandhi Intl", "Hyderabad", "India", "Asia/Kolkata"),
				new Airport("CCU", "Netaji Subhas Chandra Bose Intl", "Kolkata", "India", "Asia/Kolkata"),
				new Airport("GOI", "Goa Intl (Dabolim)", "Goa", "India", "Asia/Kolkata"),
				new Airport("DXB", "Dubai Intl", "Dubai", "UAE", "Asia/Dubai"),
				new Airport("SIN", "Changi", "Singapore", "Singapore", "Asia/Singapore"),
				new Airport("LHR", "Heathrow", "London", "UK", "Europe/London")));

		log.info("Seeded {} airports", airports.size());
	}
}
