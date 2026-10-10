import { cacheLife } from "next/cache";

import type { Airport, FlightSearchParams, FlightSearchResponse } from "./types";

// On free hosting the backend sleeps when idle and needs up to a minute to wake up.
// Give up a bit before the platform's function time limit (maxDuration in page.tsx),
// so the user gets a friendly message instead of a platform timeout page.
const REQUEST_TIMEOUT_MS = 45_000;

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
    response = await fetch(`${baseUrl}${path}`, { signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS) });
  } catch (error) {
    if (error instanceof Error && error.name === "TimeoutError") {
      throw new ApiError(
        "The SkyBook server is waking up (free hosting sleeps when idle). Please try again in about a minute.",
      );
    }
    throw new ApiError(`Cannot reach the SkyBook API at ${baseUrl}. Is the backend running?`);
  }

  if (!response.ok) {
    throw new ApiError(await errorMessage(response), response.status);
  }
  return response.json() as Promise<T>;
}

/** RFC 9457 problem details, as returned by the backend's GlobalExceptionHandler. */
type ProblemDetail = {
  title?: string;
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
};

async function errorMessage(response: Response): Promise<string> {
  const problem: ProblemDetail | null = await response.json().catch(() => null);
  const detail = problem?.detail ?? `Request failed with status ${response.status}`;
  const fieldErrors = Object.entries(problem?.errors ?? {}).map(([field, msg]) => `${field} ${msg}`);
  return fieldErrors.length > 0 ? `${detail}: ${fieldErrors.join(", ")}` : detail;
}

// Airports rarely change, so cache them: the search form renders instantly from the cache,
// even while the backend is asleep. Errors are not cached, so a failed call is retried next time.
async function getAirportsCached(): Promise<Airport[]> {
  "use cache";
  cacheLife("hours");
  return getJson<Airport[]>("/api/airports");
}

export async function getAirports(): Promise<Airport[]> {
  try {
    return await getAirportsCached();
  } catch (error) {
    // Errors thrown inside a "use cache" function lose their class when they cross the cache
    // boundary, so turn them back into an ApiError (keeping the message) for the callers.
    throw new ApiError(error instanceof Error ? error.message : "Could not load airports.");
  }
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
