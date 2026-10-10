package com.skybook.flight;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import com.skybook.airport.Airport;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "flights", indexes = @Index(name = "idx_flights_route_departure",
		columnList = "origin_airport_id, destination_airport_id, departure_time"))
public class Flight {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** e.g. "6E-2001". Not unique: the same flight number operates every day. */
	@Column(nullable = false, length = 10)
	private String flightNumber;

	@Column(nullable = false)
	private String airline;

	// LAZY: @ManyToOne defaults to EAGER, which would load both airports with every flight
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "origin_airport_id")
	private Airport origin;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "destination_airport_id")
	private Airport destination;

	// Instant = exact point in time (UTC). Convert to local time with the airport's zone for display.
	@Column(nullable = false)
	private Instant departureTime;

	@Column(nullable = false)
	private Instant arrivalTime;

	// Never double for money: 0.1 + 0.2 != 0.3 in binary floating point
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal baseFare;

	@Column(nullable = false)
	private int totalSeats;

	@Column(nullable = false)
	private int availableSeats;

	protected Flight() {
	}

	public Flight(String flightNumber, String airline, Airport origin, Airport destination,
			Instant departureTime, Instant arrivalTime, BigDecimal baseFare, int totalSeats) {
		// Compare by business key: Airport doesn't override equals() (see Airport for why)
		if (origin.getCode().equals(destination.getCode())) {
			throw new IllegalArgumentException("Origin and destination must differ");
		}
		if (!arrivalTime.isAfter(departureTime)) {
			throw new IllegalArgumentException("Arrival must be after departure");
		}
		this.flightNumber = flightNumber;
		this.airline = airline;
		this.origin = origin;
		this.destination = destination;
		this.departureTime = departureTime;
		this.arrivalTime = arrivalTime;
		this.baseFare = baseFare;
		this.totalSeats = totalSeats;
		this.availableSeats = totalSeats;
	}

	/** Correct across time zones because both ends are Instants. */
	public Duration getDuration() {
		return Duration.between(departureTime, arrivalTime);
	}

	public Long getId() {
		return id;
	}

	public String getFlightNumber() {
		return flightNumber;
	}

	public String getAirline() {
		return airline;
	}

	public Airport getOrigin() {
		return origin;
	}

	public Airport getDestination() {
		return destination;
	}

	public Instant getDepartureTime() {
		return departureTime;
	}

	public Instant getArrivalTime() {
		return arrivalTime;
	}

	public BigDecimal getBaseFare() {
		return baseFare;
	}

	public int getTotalSeats() {
		return totalSeats;
	}

	public int getAvailableSeats() {
		return availableSeats;
	}
}
