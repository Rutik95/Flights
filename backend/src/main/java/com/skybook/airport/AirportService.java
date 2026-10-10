package com.skybook.airport;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AirportService {

	private final AirportRepository airportRepository;

	public AirportService(AirportRepository airportRepository) {
		this.airportRepository = airportRepository;
	}

	@Transactional(readOnly = true)
	public List<AirportResponse> findAll() {
		return airportRepository.findAll(Sort.by("city")).stream()
				.map(AirportResponse::from)
				.toList();
	}
}
