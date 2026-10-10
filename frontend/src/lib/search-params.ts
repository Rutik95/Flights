import type { FlightSearchParams, FlightSort } from "./types";

/** What Next.js gives us: every query param may be missing, a string, or repeated (string[]). */
export type RawSearchParams = Record<string, string | string[] | undefined>;

const SORTS: FlightSort[] = ["price", "departure", "duration"];

function first(value: string | string[] | undefined): string | undefined {
  const v = Array.isArray(value) ? value[0] : value;
  return v?.trim() || undefined; // treat "" (empty form field) as missing
}

/**
 * The URL is the single source of truth for the search (like route.query in Nuxt).
 * Returns null when the required fields aren't there yet, e.g. on first visit.
 */
export function parseSearchParams(raw: RawSearchParams): FlightSearchParams | null {
  const origin = first(raw.origin);
  const destination = first(raw.destination);
  const date = first(raw.date);
  if (!origin || !destination || !date) {
    return null;
  }

  const passengers = Number(first(raw.passengers) ?? 1);
  const sort = first(raw.sort) as FlightSort | undefined;

  return {
    origin,
    destination,
    date,
    returnDate: first(raw.returnDate),
    passengers: Number.isInteger(passengers) && passengers > 0 ? passengers : 1,
    sort: sort && SORTS.includes(sort) ? sort : "price",
  };
}
