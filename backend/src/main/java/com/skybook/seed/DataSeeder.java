package com.skybook.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.skybook.airport.Airport;
import com.skybook.airport.AirportRepository;
import com.skybook.flight.Flight;
import com.skybook.flight.FlightRepository;

/**
 * Loads sample data on startup. Idempotent, so restarts don't create duplicates:
 * airports are seeded once; flights are topped up so there are always flights for the next
 * {@value #DAYS_TO_SEED} days (important for a long-running deployment, not just a fresh database).
 */
@Component
public class DataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private static final int DAYS_TO_SEED = 30;

	/** Days before departure that count as "last minute" (more expensive). */
	private static final int LAST_MINUTE_DAYS = 3;
	private static final BigDecimal LAST_MINUTE_FACTOR = new BigDecimal("1.25");

	/** Local departure times at the origin airport, with a fare factor per time of day. */
	private static final List<DepartureSlot> DEPARTURE_SLOTS = List.of(
			new DepartureSlot(LocalTime.of(6, 0), new BigDecimal("1.00")),
			new DepartureSlot(LocalTime.of(13, 30), new BigDecimal("0.90")),
			new DepartureSlot(LocalTime.of(20, 15), new BigDecimal("1.15")));

	/** Each route is seeded in both directions so round-trip searches have return flights. */
	private static final List<Route> ROUTES = List.of(
			new Route("BOM", "DEL", "IndiGo", "6E", 2001, Duration.ofMinutes(130), new BigDecimal("5500"), 180),
			new Route("BOM", "DEL", "Air India", "AI", 801, Duration.ofMinutes(135), new BigDecimal("6100"), 160),
			new Route("BLR", "DEL", "IndiGo", "6E", 2101, Duration.ofMinutes(165), new BigDecimal("6200"), 180),
			new Route("BOM", "BLR", "Akasa Air", "QP", 1301, Duration.ofMinutes(105), new BigDecimal("4200"), 186),
			new Route("BOM", "GOI", "IndiGo", "6E", 5201, Duration.ofMinutes(70), new BigDecimal("3800"), 180),
			new Route("DEL", "CCU", "Air India", "AI", 701, Duration.ofMinutes(140), new BigDecimal("5200"), 160),
			new Route("MAA", "HYD", "SpiceJet", "SG", 3101, Duration.ofMinutes(75), new BigDecimal("3300"), 189),
			new Route("BOM", "DXB", "Emirates", "EK", 501, Duration.ofMinutes(195), new BigDecimal("15500"), 350),
			new Route("DEL", "DXB", "IndiGo", "6E", 1401, Duration.ofMinutes(225), new BigDecimal("14000"), 180),
			new Route("BLR", "SIN", "Singapore Airlines", "SQ", 501, Duration.ofMinutes(270), new BigDecimal("18500"), 300),
			new Route("BOM", "LHR", "British Airways", "BA", 101, Duration.ofMinutes(590), new BigDecimal("48000"), 280));

	private final AirportRepository airportRepository;
	private final FlightRepository flightRepository;

	public DataSeeder(AirportRepository airportRepository, FlightRepository flightRepository) {
		this.airportRepository = airportRepository;
		this.flightRepository = flightRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		seedAirports();
		seedFlights();
	}

	private void seedAirports() {
		if (airportRepository.count() > 0) {
			log.info("Airports already seeded, skipping");
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

	private void seedFlights() {
		Map<String, Airport> airportsByCode = airportRepository.findAll().stream()
				.collect(Collectors.toMap(Airport::getCode, Function.identity()));

		Instant now = Instant.now();
		List<Flight> flights = new ArrayList<>();
		for (Route route : ROUTES) {
			Airport a = airport(airportsByCode, route.from());
			Airport b = airport(airportsByCode, route.to());
			// Outbound gets odd flight numbers (2001, 2003...), return gets even (2002, 2004...)
			addDailyFlights(flights, route, a, b, route.flightNumberBase(), now);
			addDailyFlights(flights, route, b, a, route.flightNumberBase() + 1, now);
		}

		if (flights.isEmpty()) {
			log.info("Flights already scheduled for the next {} days", DAYS_TO_SEED);
			return;
		}
		flightRepository.saveAll(flights);
		log.info("Added {} flights to keep the next {} days scheduled", flights.size(), DAYS_TO_SEED);
	}

	private void addDailyFlights(List<Flight> flights, Route route, Airport origin, Airport destination,
			int flightNumberBase, Instant now) {
		ZoneId originZone = origin.getZone();
		LocalDate today = LocalDate.now(originZone);
		LocalDate endExclusive = today.plusDays(DAYS_TO_SEED);

		// Continue after the last day already in the database (in the origin's local date),
		// or start today if this route has no flights yet or the data is stale.
		LocalDate firstDate = flightRepository
				.findLatestDeparture(route.airline(), origin.getCode(), destination.getCode())
				.map(latest -> latest.atZone(originZone).toLocalDate().plusDays(1))
				.filter(next -> next.isAfter(today))
				.orElse(today);

		for (LocalDate date = firstDate; date.isBefore(endExclusive); date = date.plusDays(1)) {
			boolean lastMinute = ChronoUnit.DAYS.between(today, date) < LAST_MINUTE_DAYS;
			for (int i = 0; i < DEPARTURE_SLOTS.size(); i++) {
				DepartureSlot slot = DEPARTURE_SLOTS.get(i);

				// "06:00 on this date in Mumbai" -> exact instant in UTC
				Instant departure = date.atTime(slot.localTime()).atZone(originZone).toInstant();
				if (departure.isBefore(now)) {
					continue; // today's earlier flights have already left
				}

				BigDecimal fare = route.baseFare()
						.multiply(slot.fareFactor())
						.multiply(lastMinute ? LAST_MINUTE_FACTOR : BigDecimal.ONE)
						.setScale(0, RoundingMode.HALF_UP) // whole rupees
						.setScale(2);

				flights.add(new Flight(
						route.airlineCode() + "-" + (flightNumberBase + i * 2),
						route.airline(),
						origin,
						destination,
						departure,
						departure.plus(route.duration()),
						fare,
						route.seats()));
			}
		}
	}

	private static Airport airport(Map<String, Airport> airportsByCode, String code) {
		Airport airport = airportsByCode.get(code);
		if (airport == null) {
			throw new IllegalStateException("Seed route references unknown airport: " + code);
		}
		return airport;
	}

	private record Route(String from, String to, String airline, String airlineCode, int flightNumberBase,
			Duration duration, BigDecimal baseFare, int seats) {
	}

	private record DepartureSlot(LocalTime localTime, BigDecimal fareFactor) {
	}
}
