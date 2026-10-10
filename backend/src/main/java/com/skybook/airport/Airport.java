package com.skybook.airport;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "airports")
public class Airport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** IATA code, e.g. "BOM". */
	@Column(nullable = false, unique = true, length = 3)
	private String code;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String country;

	/** IANA time zone, e.g. "Asia/Kolkata". Used to show flight times in local time. */
	@Column(nullable = false)
	private String zoneId;

	// No equals/hashCode on purpose: an id-based version breaks while id is still null (before save).
	// Compare airports by code instead.

	/** Required by JPA (Hibernate instantiates entities via reflection). Not for application code. */
	protected Airport() {
	}

	public Airport(String code, String name, String city, String country, String zoneId) {
		this.code = code;
		this.name = name;
		this.city = city;
		this.country = country;
		this.zoneId = zoneId;
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getZoneId() {
		return zoneId;
	}
}
