import { Suspense } from "react";

import { SearchForm } from "@/components/SearchForm";
import { SearchResults } from "@/components/SearchResults";

// Not async: the page itself never awaits searchParams. It passes the promise down to the
// components that need it, each inside <Suspense>. With Cache Components enabled, everything
// outside the boundaries (header, fallbacks) is prerendered as a static shell and shown
// instantly, and the request-dependent parts stream in.
export default function Home({ searchParams }: PageProps<"/">) {
  return (
    <main className="mx-auto flex w-full max-w-5xl flex-1 flex-col gap-8 px-4 py-10">
      <header>
        <h1 className="text-3xl font-bold tracking-tight">
          Sky<span className="text-sky-600">Book</span>
        </h1>
        <p className="text-sm text-zinc-500">Search and book flights</p>
      </header>

      <Suspense fallback={<div className="h-40 animate-pulse rounded-2xl bg-zinc-100 dark:bg-zinc-900" />}>
        <SearchForm searchParams={searchParams} />
      </Suspense>

      <Suspense fallback={<p className="text-center text-sm text-zinc-500">Searching flights…</p>}>
        <SearchResults searchParams={searchParams} />
      </Suspense>
    </main>
  );
}
