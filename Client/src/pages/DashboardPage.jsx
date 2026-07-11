import { useMemo, useState } from "react";
import { useVehicles } from "../hooks/useVehicles.js";
import { purchaseVehicle } from "../api/vehicleApi.js";

function DashboardPage() {
  const { vehicles, loading, error, refetch, search, page, totalPages, totalElements, goToPage } = useVehicles();
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [message, setMessage] = useState("");
  const [actionError, setActionError] = useState("");
  const [purchasingId, setPurchasingId] = useState(null);

  const categories = useMemo(
    () => Array.from(new Set(vehicles.map((vehicle) => vehicle.category || "Unspecified"))),
    [vehicles]
  );

  const totalValue = useMemo(
    () => vehicles.reduce((sum, vehicle) => sum + Number(vehicle.price ?? 0) * (vehicle.quantity ?? 0), 0),
    [vehicles]
  );

  const inStockCount = useMemo(
    () => vehicles.filter((vehicle) => vehicle.quantity > 0).length,
    [vehicles]
  );

  const handleSearch = async (event) => {
    event?.preventDefault();
    setMessage("");
    setActionError("");

    const params = {};
    if (query.trim()) {
      params.make = query.trim();
      params.model = query.trim();
    }
    if (category) params.category = category;
    if (minPrice.trim()) params.minPrice = minPrice;
    if (maxPrice.trim()) params.maxPrice = maxPrice;

    await search(params, { page: 0 });
  };

  const handlePurchase = async (vehicleId) => {
    setActionError("");
    setMessage("");
    setPurchasingId(vehicleId);

    try {
      await purchaseVehicle(vehicleId);
      setMessage("Purchase successful! Inventory updated.");
      refetch();
    } catch (err) {
      setActionError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          "Unable to complete purchase. Please try again."
      );
    } finally {
      setPurchasingId(null);
    }
  };

  return (
    <div className="space-y-8">
      <header className="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm shadow-slate-200/50">
        <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.3em] text-sky-600">
              Inventory dashboard
            </p>
            <h1 className="mt-2 text-3xl font-semibold text-slate-900 sm:text-4xl">
              Available vehicles & stock overview
            </h1>
            <p className="mt-3 max-w-2xl text-sm text-slate-500 sm:text-base">
              Browse available vehicles, filter by category or price, and manage purchases from one responsive dashboard.
            </p>
          </div>

          <div className="grid w-full gap-3 sm:grid-cols-3 md:w-auto">
            <button
              type="button"
              onClick={refetch}
              className="inline-flex items-center justify-center rounded-2xl bg-slate-900 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
            >
              Refresh inventory
            </button>
            <div className="rounded-2xl bg-slate-900/5 px-4 py-3 text-sm text-slate-700">
              <div className="font-semibold text-slate-900">Total models</div>
              <div>{vehicles.length}</div>
            </div>
            <div className="rounded-2xl bg-slate-900/5 px-4 py-3 text-sm text-slate-700">
              <div className="font-semibold text-slate-900">In stock</div>
              <div>{inStockCount}</div>
            </div>
          </div>
        </div>

        <div className="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <div className="rounded-3xl bg-sky-600 px-6 py-5 text-white shadow-lg shadow-sky-300/10">
            <p className="text-sm uppercase tracking-[0.25em] text-sky-200">Inventory value</p>
            <p className="mt-4 text-3xl font-semibold">${totalValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
          </div>
          <div className="rounded-3xl bg-white px-6 py-5 shadow-lg shadow-slate-200/80">
            <p className="text-sm uppercase tracking-[0.25em] text-slate-500">Unique categories</p>
            <p className="mt-4 text-3xl font-semibold text-slate-900">{categories.length}</p>
          </div>
          <div className="rounded-3xl bg-white px-6 py-5 shadow-lg shadow-slate-200/80">
            <p className="text-sm uppercase tracking-[0.25em] text-slate-500">High-demand stock</p>
            <p className="mt-4 text-3xl font-semibold text-emerald-600">{inStockCount >= vehicles.length ? "Healthy" : "Monitor"}</p>
          </div>
        </div>
      </header>

      <section className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <article className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/50">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="text-xl font-semibold text-slate-900">Search inventory</h2>
              <p className="mt-2 text-sm text-slate-500">
                Filter vehicles by model, category, or price range.
              </p>
            </div>
          </div>

          <form onSubmit={handleSearch} className="mt-6 space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <label className="block">
                <span className="text-sm font-medium text-slate-700">Search</span>
                <input
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  placeholder="Make, model, or keyword"
                  className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                />
              </label>
              <label className="block">
                <span className="text-sm font-medium text-slate-700">Category</span>
                <select
                  value={category}
                  onChange={(event) => setCategory(event.target.value)}
                  className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                >
                  <option value="">All categories</option>
                  {categories.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <label className="block">
                <span className="text-sm font-medium text-slate-700">Min price</span>
                <input
                  type="number"
                  min="0"
                  value={minPrice}
                  onChange={(event) => setMinPrice(event.target.value)}
                  placeholder="0"
                  className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                />
              </label>
              <label className="block">
                <span className="text-sm font-medium text-slate-700">Max price</span>
                <input
                  type="number"
                  min="0"
                  value={maxPrice}
                  onChange={(event) => setMaxPrice(event.target.value)}
                  placeholder="99999"
                  className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                />
              </label>
            </div>

            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <button
                type="submit"
                className="inline-flex items-center justify-center rounded-2xl bg-sky-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-sky-700"
              >
                Apply filters
              </button>
              <button
                type="button"
                onClick={() => {
                  setQuery("");
                  setCategory("");
                  setMinPrice("");
                  setMaxPrice("");
                  refetch();
                }}
                className="inline-flex items-center justify-center rounded-2xl border border-slate-200 bg-white px-5 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50"
              >
                Reset filters
              </button>
            </div>
          </form>

          {(error || actionError) && (
            <div className="mt-6 rounded-3xl border border-rose-200 bg-rose-50 px-4 py-4 text-sm text-rose-900">
              {actionError || "Failed to load vehicles. Please try again later."}
            </div>
          )}

          {message && (
            <div className="mt-6 rounded-3xl border border-emerald-200 bg-emerald-50 px-4 py-4 text-sm text-emerald-900">
              {message}
            </div>
          )}
        </article>

        <aside className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/50">
          <h2 className="text-xl font-semibold text-slate-900">Dashboard highlights</h2>
          <div className="mt-6 space-y-4 text-sm text-slate-600">
            <div className="rounded-3xl bg-slate-50 p-4">
              <p className="font-semibold text-slate-900">Quick tips</p>
              <p className="mt-2 leading-relaxed">Use the filters to narrow results by category or price, then purchase directly from the vehicle cards.</p>
            </div>
            <div className="rounded-3xl bg-slate-50 p-4">
              <p className="font-semibold text-slate-900">Stock status</p>
              <p className="mt-2 leading-relaxed">Vehicles with no stock are clearly marked, so you can avoid selling unavailable cars.</p>
            </div>
            <div className="rounded-3xl bg-slate-50 p-4">
              <p className="font-semibold text-slate-900">Inventory refresh</p>
              <p className="mt-2 leading-relaxed">Press refresh any time to load the latest stock and category counts.</p>
            </div>
          </div>
        </aside>
      </section>

      <section>
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-2xl font-semibold text-slate-900">Vehicle catalog</h2>
            <p className="mt-2 text-sm text-slate-500">Browse all available inventory items with pricing, availability, and purchase actions.</p>
          </div>
          <div className="text-sm text-slate-500">
            {loading ? "Loading vehicles..." : `${totalElements || vehicles.length} vehicles shown`}
          </div>
        </div>

        <div className="grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
          {loading ? (
            Array.from({ length: 6 }).map((_, index) => (
              <div key={index} className="animate-pulse rounded-3xl border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/50">
                <div className="h-5 w-3/4 rounded-xl bg-slate-200"></div>
                <div className="mt-5 space-y-3">
                  <div className="h-4 w-1/2 rounded-xl bg-slate-200"></div>
                  <div className="h-4 rounded-xl bg-slate-200"></div>
                  <div className="h-10 rounded-2xl bg-slate-200"></div>
                </div>
              </div>
            ))
          ) : vehicles.length > 0 ? (
            vehicles.map((vehicle) => (
              <article
                key={vehicle.id}
                className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm shadow-slate-200/50 transition hover:-translate-y-1 hover:border-sky-200"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-sm font-semibold uppercase tracking-[0.25em] text-sky-600">
                      {vehicle.category || "General"}
                    </p>
                    <h3 className="mt-2 text-xl font-semibold text-slate-900">
                      {vehicle.make} {vehicle.model}
                    </h3>
                  </div>
                  <div className="rounded-3xl bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700">
                    {vehicle.quantity > 0 ? "In stock" : "Out of stock"}
                  </div>
                </div>

                <div className="mt-6 grid gap-4 sm:grid-cols-2">
                  <div className="rounded-3xl bg-slate-50 p-4">
                    <p className="text-sm text-slate-500">Price</p>
                    <p className="mt-2 text-lg font-semibold text-slate-900">${Number(vehicle.price).toLocaleString()}</p>
                  </div>
                  <div className="rounded-3xl bg-slate-50 p-4">
                    <p className="text-sm text-slate-500">Quantity</p>
                    <p className="mt-2 text-lg font-semibold text-slate-900">{vehicle.quantity}</p>
                  </div>
                </div>

                <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div className="text-sm text-slate-500">
                    ID: <span className="font-medium text-slate-700">{vehicle.id}</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => handlePurchase(vehicle.id)}
                    disabled={vehicle.quantity <= 0 || purchasingId === vehicle.id}
                    className="inline-flex items-center justify-center rounded-2xl bg-sky-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-sky-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                  >
                    {vehicle.quantity <= 0
                      ? "Unavailable"
                      : purchasingId === vehicle.id
                      ? "Purchasing..."
                      : "Buy 1 unit"}
                  </button>
                </div>
              </article>
            ))
          ) : (
            <div className="rounded-3xl border border-slate-200 bg-white p-10 text-center shadow-sm shadow-slate-200/50">
              <p className="text-lg font-semibold text-slate-900">No vehicles match your filters.</p>
              <p className="mt-2 text-sm text-slate-500">Try resetting filters or refreshing inventory.</p>
            </div>
          )}
        </div>

        {/* Pagination controls */}
        <div className="mt-6 flex items-center justify-center">
          <nav className="inline-flex items-center gap-2 rounded-2xl bg-white/80 p-2 shadow-sm">
            <button
              onClick={() => goToPage(Math.max(0, page - 1))}
              disabled={page <= 0}
              className="px-3 py-1 rounded-md bg-slate-100 text-sm disabled:opacity-50"
            >
              Prev
            </button>

            {Array.from({ length: Math.max(1, totalPages) }).map((_, idx) => {
              // show only a window of pages when many
              const start = Math.max(0, page - 2);
              const end = Math.min(totalPages - 1, start + 4);
              if (idx < start || idx > end) return null;
              return (
                <button
                  key={idx}
                  onClick={() => goToPage(idx)}
                  className={`px-3 py-1 rounded-md text-sm ${idx === page ? "bg-sky-600 text-white" : "bg-white"}`}
                >
                  {idx + 1}
                </button>
              );
            })}

            <button
              onClick={() => goToPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              className="px-3 py-1 rounded-md bg-slate-100 text-sm disabled:opacity-50"
            >
              Next
            </button>
          </nav>
        </div>
      </section>
    </div>
  );
}

export default DashboardPage;
