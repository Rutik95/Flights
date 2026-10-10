import Form from "next/form";

import { ApiError, getAirports } from "@/lib/api";
import type { RawSearchParams } from "@/lib/search-params";

const inputClass =
  "w-full rounded-lg border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 " +
  "focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/20 " +
  "dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";

const labelClass = "mb-1 block text-xs font-medium uppercase tracking-wide text-zinc-500";

// Server Component: fetches the airport list on the server and renders plain HTML.
// <Form> from next/form submits as GET to the same page, i.e. it just updates the URL
// (?origin=BOM&destination=DEL&...) using client-side navigation, no full reload.
export async function SearchForm({ searchParams }: { searchParams: Promise<RawSearchParams> }) {
  const params = await searchParams;
  const value = (key: string) => {
    const v = params[key];
    return (Array.isArray(v) ? v[0] : v) ?? "";
  };

  let airports;
  try {
    airports = await getAirports();
  } catch (error) {
    const message = error instanceof ApiError ? error.message : "Could not load airports.";
    return <p className="rounded-lg bg-red-50 p-4 text-sm text-red-700">{message}</p>;
  }

  const airportOptions = airports.map((a) => (
    <option key={a.code} value={a.code}>
      {a.city} ({a.code})
    </option>
  ));

  return (
    <Form
      action=""
      className="grid grid-cols-2 gap-4 rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm md:grid-cols-6 dark:border-zinc-800 dark:bg-zinc-950"
    >
      <div className="col-span-1 md:col-span-1">
        <label htmlFor="origin" className={labelClass}>From</label>
        <select id="origin" name="origin" required defaultValue={value("origin")} className={inputClass}>
          <option value="" disabled>Select</option>
          {airportOptions}
        </select>
      </div>

      <div className="col-span-1 md:col-span-1">
        <label htmlFor="destination" className={labelClass}>To</label>
        <select id="destination" name="destination" required defaultValue={value("destination")} className={inputClass}>
          <option value="" disabled>Select</option>
          {airportOptions}
        </select>
      </div>

      <div className="col-span-1">
        <label htmlFor="date" className={labelClass}>Depart</label>
        <input id="date" name="date" type="date" required defaultValue={value("date")} className={inputClass} />
      </div>

      <div className="col-span-1">
        <label htmlFor="returnDate" className={labelClass}>Return (optional)</label>
        <input id="returnDate" name="returnDate" type="date" defaultValue={value("returnDate")} className={inputClass} />
      </div>

      <div className="col-span-1">
        <label htmlFor="passengers" className={labelClass}>Passengers</label>
        <input
          id="passengers"
          name="passengers"
          type="number"
          min={1}
          max={9}
          defaultValue={value("passengers") || "1"}
          className={inputClass}
        />
      </div>

      <div className="col-span-1">
        <label htmlFor="sort" className={labelClass}>Sort by</label>
        <select id="sort" name="sort" defaultValue={value("sort") || "price"} className={inputClass}>
          <option value="price">Cheapest</option>
          <option value="departure">Earliest</option>
          <option value="duration">Fastest</option>
        </select>
      </div>

      <button
        type="submit"
        className="col-span-2 rounded-lg bg-sky-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-sky-700 md:col-span-6"
      >
        Search flights
      </button>
    </Form>
  );
}
