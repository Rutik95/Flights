// The API sends times as local airport time with an offset ("2026-10-15T13:30:00+05:30").
// We read the local part directly from the string instead of using new Date(): a Date would be
// converted to the timezone of whoever runs the code (the server, or the user's browser),
// but a departure time should always be shown in the airport's own local time.

/** "2026-10-15T13:30:00+05:30" -> "13:30" */
export function formatTime(isoWithOffset: string): string {
  return isoWithOffset.slice(11, 16);
}

/** "2026-10-15T13:30:00+05:30" -> "Thu, 15 Oct" */
export function formatDate(isoWithOffset: string): string {
  const localDate = isoWithOffset.slice(0, 10);
  // Parse the plain date as UTC midnight and format it in UTC, so no timezone shift can happen
  return new Date(`${localDate}T00:00:00Z`).toLocaleDateString("en-IN", {
    timeZone: "UTC",
    weekday: "short",
    day: "numeric",
    month: "short",
  });
}

/** Number of calendar days between the local departure and arrival dates (for the "+1" badge). */
export function dayDifference(departure: string, arrival: string): number {
  const dep = Date.parse(`${departure.slice(0, 10)}T00:00:00Z`);
  const arr = Date.parse(`${arrival.slice(0, 10)}T00:00:00Z`);
  return Math.round((arr - dep) / 86_400_000);
}

/** 590 -> "9h 50m" */
export function formatDuration(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return m === 0 ? `${h}h` : `${h}h ${m}m`;
}

const inr = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 0,
});

/** 48000 -> "₹48,000" */
export function formatPrice(amount: number): string {
  return inr.format(amount);
}
