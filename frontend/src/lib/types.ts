// TypeScript mirrors of the backend DTOs (com.skybook.*Response records).
// Keep these in sync with the Java records: the compiler can't check across the HTTP boundary.

export type Airport = {
  code: string;
  name: string;
  city: string;
  country: string;
};

export type Flight = {
  id: number;
  flightNumber: string;
  airline: string;
  origin: Airport;
  destination: Airport;
  /** Local time at the origin airport with UTC offset, e.g. "2026-10-15T13:30:00+05:30" */
  departureTime: string;
  /** Local time at the destination airport with UTC offset */
  arrivalTime: string;
  durationMinutes: number;
  /** Per passenger, in INR. Sent as a JSON number (BigDecimal on the backend). */
  baseFare: number;
  seatsAvailable: number;
};

export type FlightSearchResponse = {
  outbound: Flight[];
  /** Empty for one-way searches (never null) */
  inbound: Flight[];
};

export type FlightSort = "price" | "departure" | "duration";

export type FlightSearchParams = {
  origin: string;
  destination: string;
  date: string;
  returnDate?: string;
  passengers: number;
  sort: FlightSort;
};
