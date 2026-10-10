import { dayDifference, formatDuration, formatPrice, formatTime } from "@/lib/format";
import type { Flight } from "@/lib/types";

// No "use client": this component has no state or event handlers, so it stays a Server
// Component and ships zero JavaScript to the browser.
export function FlightCard({ flight, passengers }: { flight: Flight; passengers: number }) {
  const extraDays = dayDifference(flight.departureTime, flight.arrivalTime);

  return (
    <li className="flex flex-col gap-4 rounded-xl border border-zinc-200 bg-white p-4 sm:flex-row sm:items-center dark:border-zinc-800 dark:bg-zinc-950">
      <div className="sm:w-40">
        <p className="font-semibold text-zinc-900 dark:text-zinc-100">{flight.airline}</p>
        <p className="text-xs text-zinc-500">{flight.flightNumber}</p>
      </div>

      <div className="flex flex-1 items-center gap-3">
        <div className="text-center">
          <p className="text-xl font-semibold tabular-nums">{formatTime(flight.departureTime)}</p>
          <p className="text-xs text-zinc-500">{flight.origin.code}</p>
        </div>

        <div className="flex flex-1 flex-col items-center">
          <p className="text-xs text-zinc-500">{formatDuration(flight.durationMinutes)}</p>
          <div className="my-1 h-px w-full bg-zinc-300 dark:bg-zinc-700" />
          <p className="text-xs text-zinc-400">Non-stop</p>
        </div>

        <div className="text-center">
          <p className="text-xl font-semibold tabular-nums">
            {formatTime(flight.arrivalTime)}
            {extraDays > 0 && <sup className="ml-0.5 text-xs font-medium text-amber-600">+{extraDays}</sup>}
          </p>
          <p className="text-xs text-zinc-500">{flight.destination.code}</p>
        </div>
      </div>

      <div className="text-right sm:w-40">
        <p className="text-xl font-bold text-zinc-900 dark:text-zinc-100">{formatPrice(flight.baseFare)}</p>
        <p className="text-xs text-zinc-500">
          {passengers > 1 ? `${formatPrice(flight.baseFare * passengers)} for ${passengers}` : "per passenger"}
        </p>
        {flight.seatsAvailable < 10 && (
          <p className="text-xs font-medium text-red-600">Only {flight.seatsAvailable} seats left</p>
        )}
      </div>
    </li>
  );
}
