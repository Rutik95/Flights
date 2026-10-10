package com.skybook.flight;

import java.util.List;

/**
 * Search results. For one-way searches {@code inbound} is an empty list (never null),
 * so clients don't need null checks.
 */
public record FlightSearchResponse(List<FlightResponse> outbound, List<FlightResponse> inbound) {
}
