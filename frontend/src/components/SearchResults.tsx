import { FlightCard } from "@/components/FlightCard";
import { ApiError, searchFlights } from "@/lib/api";
import { formatDate } from "@/lib/format";
import { parseSearchParams, type RawSearchParams } from "@/lib/search-params";
import type { Flight, FlightSearchResponse } from "@/lib/types";

// Server Component: runs on the Next.js server, calls Spring Boot, returns rendered HTML.
// It reads searchParams (request-time data), so page.tsx wraps it in <Suspense>.
export async function SearchResults({ searchParams }: { searchParams: Promise<RawSearchParams> }) {
  const params = parseSearchParams(await searchParams);
  if (!params) {
    return <p className="text-center text-sm text-zinc-500">Choose a route and date to see flights.</p>;
  }

  let result: FlightSearchResponse;
  try {
    result = await searchFlights(params);
  } catch (error) {
    const message = error instanceof ApiError ? error.message : "Something went wrong.";
    return <p className="rounded-lg bg-red-50 p-4 text-sm text-red-700">{message}</p>;
  }

  return (
    <div className="flex flex-col gap-8">
      <FlightList title="Outbound" flights={result.outbound} passengers={params.passengers} />
      {params.returnDate && (
        <FlightList title="Return" flights={result.inbound} passengers={params.passengers} />
      )}
    </div>
  );
}

function FlightList({ title, flights, passengers }: { title: string; flights: Flight[]; passengers: number }) {
  if (flights.length === 0) {
    return (
      <section>
        <h2 className="mb-3 text-lg font-semibold">{title}</h2>
        <p className="rounded-lg bg-zinc-100 p-4 text-sm text-zinc-600 dark:bg-zinc-900 dark:text-zinc-400">
          No flights found for this date and number of passengers.
        </p>
      </section>
    );
  }

  const { origin, destination, departureTime } = flights[0];
  return (
    <section>
      <h2 className="mb-3 text-lg font-semibold">
        {title}: {origin.city} → {destination.city}
        <span className="ml-2 text-sm font-normal text-zinc-500">
          {formatDate(departureTime)} · {flights.length} flights
        </span>
      </h2>
      <ul className="flex flex-col gap-3">
        {flights.map((flight) => (
          <FlightCard key={flight.id} flight={flight} passengers={passengers} />
        ))}
      </ul>
    </section>
  );
}
