import type { Airport, FlightSearchParams, FlightSearchResponse } from "./types";

// Server-side only: these functions are called from Server Components, so the browser never
// talks to the Spring Boot API directly (and we need no CORS setup yet).
// No NEXT_PUBLIC_ prefix on purpose: this variable is not exposed to the browser bundle.
// Read at request time (not build time), so one Docker image works in every environment:
// http://localhost:8080 in dev, http://backend:8080 inside Docker Compose.
function apiBaseUrl(): string {
  return process.env.API_BASE_URL ?? "http://localhost:8080";
}

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status?: number,
  ) {
    super(message);
  }
}

async function getJson<T>(path: string): Promise<T> {
  const baseUrl = apiBaseUrl();
  let response: Response;
  try {
    response = await fetch(`${baseUrl}${path}`);
  } catch {
    throw new ApiError(`Cannot reach the SkyBook API at ${baseUrl}. Is the backend running?`);
  }

  if (!response.ok) {
    // Spring's error JSON has a "message" field (we'll standardise this in Step 2)
    const body = await response.json().catch(() => null);
    throw new ApiError(body?.message ?? `Request failed with status ${response.status}`, response.status);
  }
  return response.json() as Promise<T>;
}

export function getAirports(): Promise<Airport[]> {
  return getJson<Airport[]>("/api/airports");
}

export function searchFlights(params: FlightSearchParams): Promise<FlightSearchResponse> {
  const query = new URLSearchParams({
    origin: params.origin,
    destination: params.destination,
    date: params.date,
    passengers: String(params.passengers),
    sort: params.sort,
  });
  if (params.returnDate) {
    query.set("returnDate", params.returnDate);
  }
  return getJson<FlightSearchResponse>(`/api/flights/search?${query}`);
}
