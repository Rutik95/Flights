package com.skybook.flight;

import java.util.Locale;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Lets clients send ?sort=price as well as ?sort=PRICE. Spring's default String-to-enum
 * conversion is case-sensitive. Spring Boot registers Converter beans with Spring MVC automatically.
 */
@Component
public class StringToFlightSortConverter implements Converter<String, FlightSort> {

	@Override
	public FlightSort convert(String source) {
		return FlightSort.valueOf(source.trim().toUpperCase(Locale.ROOT));
	}
}
