import { useEffect, useMemo, useState } from "react";
import { useVehicles } from "../hooks/useVehicles.js";
import {
  createVehicle,
  updateVehicle,
  deleteVehicle,
  restockVehicle,
} from "../api/vehicleApi.js";

function AdminPage() {
  const { vehicles, loading, error, refetch, page, totalPages, goToPage } = useVehicles();

  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ make: "", model: "", category: "", price: "", quantity: "" });
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    if (!showForm) setEditing(null);
  }, [showForm]);

  const resetForm = () => setForm({ make: "", model: "", category: "", price: "", quantity: "" });

  const startEdit = (v) => {
    setEditing(v);
    setForm({ make: v.make, model: v.model, category: v.category, price: v.price, quantity: v.quantity });
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setMsg("");
    try {
      const payload = { make: form.make, model: form.model, category: form.category, price: Number(form.price), quantity: Number(form.quantity) };
      if (editing) {
        await updateVehicle(editing.id, payload);
        setMsg("Vehicle updated successfully.");
      } else {
        await createVehicle(payload);
        setMsg("Vehicle created successfully.");
      }
      resetForm();
      setShowForm(false);
      refetch();
    } catch (err) {
      setMsg(err.response?.data?.message || "Operation failed.");
    } finally {
      setBusy(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm("Delete this vehicle?")) return;
    setBusy(true);
    try {
      await deleteVehicle(id);
      setMsg("Deleted.");
      refetch();
    } catch (err) {
      setMsg("Delete failed.");
    } finally {
      setBusy(false);
    }
  };

  const handleRestock = async (id) => {
    const qty = Number(prompt("Restock quantity:", "1"));
    if (!qty || qty <= 0) return;
    setBusy(true);
    try {
      await restockVehicle(id, qty);
      setMsg("Restocked.");
      refetch();
    } catch (err) {
      setMsg("Restock failed.");
    } finally {
      setBusy(false);
    }
  };

  const categories = useMemo(() => Array.from(new Set(vehicles.map((v) => v.category || "Unspecified"))), [vehicles]);

  let vehicleCards;
  if (loading) {
    vehicleCards = (
      <div className="col-span-full grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div
            key={i}
            className="animate-pulse rounded-[28px] border border-slate-200 bg-white p-6 shadow-lg"
          >
            <div className="h-5 w-2/3 rounded bg-slate-200"></div>
            <div className="mt-6 space-y-3">
              <div className="h-4 rounded bg-slate-200"></div>
              <div className="h-4 rounded bg-slate-200"></div>
              <div className="h-10 rounded bg-slate-200"></div>
            </div>
          </div>
        ))}
      </div>
    );
  } else if (vehicles.length === 0) {
    vehicleCards = (
      <div className="col-span-full rounded-[30px] border border-slate-200 bg-white p-16 text-center shadow-lg">
        <div className="text-6xl">🚗</div>
        <h2 className="mt-5 text-2xl font-bold text-slate-900">No Vehicles Found</h2>
        <p className="mt-3 text-slate-500">
          Create your first vehicle to start managing inventory.
        </p>
      </div>
    );
  } else {
    vehicleCards = vehicles.map((v) => (
      <div key={v.id} className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-lg shadow-sky-100/40 transition-all duration-300 hover:-translate-y-2 hover:border-sky-200 hover:shadow-2xl">
        <div className="flex items-start justify-between">
          <div>
            <p className="inline-block rounded-full bg-sky-100 px-3 py-1 text-xs font-semibold uppercase tracking-wider text-sky-700">{v.category}</p>
            <h3 className="mt-3 text-2xl font-bold tracking-tight text-slate-900">{v.make} {v.model}</h3>
            <p className="mt-4 text-sm text-slate-600">Price: <span className="font-medium">${Number(v.price).toLocaleString()}</span></p>
            <p className="mt-2 text-sm text-slate-600">Qty: <span className="font-medium">{v.quantity}</span></p>
          </div>
          <div className="flex flex-col items-end gap-2">
            <button onClick={() => startEdit(v)} className="rounded-full bg-blue-100 px-4 py-2 text-sm font-semibold text-blue-700 transition hover:bg-blue-200">Edit</button>
            <button onClick={() => handleRestock(v.id)} className="rounded-full bg-emerald-100 px-4 py-2 text-sm font-semibold text-emerald-700 transition hover:bg-emerald-200">Restock</button>
            <button onClick={() => handleDelete(v.id)} className="rounded-full bg-rose-100 px-4 py-2 text-sm font-semibold text-rose-700 transition hover:bg-rose-200">Delete</button>
          </div>
        </div>
      </div>
    ));
  }

  return (
    <div className="min-h-screen space-y-8 bg-gradient-to-br from-sky-50 via-white to-blue-50 p-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Admin Dashboard</h1>
          <p className="text-sm text-slate-600">Manage vehicles: create, edit, delete, and restock inventory.</p>
        </div>
        <div className="flex items-center gap-3">
          <select className="rounded-2xl border px-3 py-2 text-sm" onChange={(e) => goToPage(Number(e.target.value))} value={page}>
            {Array.from({ length: Math.max(1, totalPages) }).map((_, i) => (
              <option key={i} value={i}>Page {i + 1}</option>
            ))}
          </select>
          <button
            onClick={() => { setShowForm((s) => !s); resetForm(); setEditing(null); }}
            className="
rounded-2xl
bg-gradient-to-r
from-sky-500
to-blue-600
px-6
py-3
font-semibold
text-white
shadow-lg
transition-all
hover:-translate-y-1
hover:shadow-xl
"
          >
            {showForm ? "Close" : "New vehicle"}
          </button>
        </div>
      </header>

      {msg && <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700 shadow-sm">{msg}</div>}

      {showForm && (
        <form
  onSubmit={handleSubmit}
  className="grid gap-6 rounded-[30px] border border-sky-100 bg-white p-8 shadow-xl shadow-sky-100/40 sm:grid-cols-2"
>
          <label className="block">
            <span className="text-sm text-slate-700">Make</span>
            <input value={form.make} onChange={(e) => setForm((f) => ({ ...f, make: e.target.value }))} className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 shadow-sm transition-all duration-300 hover:border-sky-300 hover:shadow-md focus:border-sky-500 focus:ring-4 focus:ring-sky-100 focus:shadow-lg outline-none" />
          </label>
          <label className="block">
            <span className="text-sm text-slate-700">Model</span>
            <input value={form.model} onChange={(e) => setForm((f) => ({ ...f, model: e.target.value }))} className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 shadow-sm transition-all duration-300 hover:border-sky-300 hover:shadow-md focus:border-sky-500 focus:ring-4 focus:ring-sky-100 focus:shadow-lg outline-none" />
          </label>
          <label className="block">
            <span className="text-sm text-slate-700">Category</span>
            <input value={form.category} onChange={(e) => setForm((f) => ({ ...f, category: e.target.value }))} className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 shadow-sm transition-all duration-300 hover:border-sky-300 hover:shadow-md focus:border-sky-500 focus:ring-4 focus:ring-sky-100 focus:shadow-lg outline-none" list="cats" />
            <datalist id="cats">{categories.map((c) => <option key={c} value={c} />)}</datalist>
          </label>
          <label className="block">
            <span className="text-sm text-slate-700">Price</span>
            <input type="number" value={form.price} onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))} className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 shadow-sm transition-all duration-300 hover:border-sky-300 hover:shadow-md focus:border-sky-500 focus:ring-4 focus:ring-sky-100 focus:shadow-lg outline-none" />
          </label>
          <label className="block">
            <span className="text-sm text-slate-700">Quantity</span>
            <input type="number" value={form.quantity} onChange={(e) => setForm((f) => ({ ...f, quantity: e.target.value }))} className="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 shadow-sm transition-all duration-300 hover:border-sky-300 hover:shadow-md focus:border-sky-500 focus:ring-4 focus:ring-sky-100 focus:shadow-lg outline-none" />
          </label>

          <div className="sm:col-span-2 mt-3 flex gap-4">
            <button disabled={busy} type="submit" className="rounded-2xl bg-gradient-to-r from-sky-500 to-blue-600 px-6 py-3 font-semibold text-white shadow-lg transition-all duration-300 hover:-translate-y-1 hover:shadow-xl disabled:opacity-60">{editing ? "Save" : "Create"}</button>
            <button type="button" onClick={() => { resetForm(); setShowForm(false); }} className="rounded-2xl border border-slate-200 bg-slate-50 px-6 py-3 font-semibold text-slate-700 shadow-sm transition-all duration-300 hover:border-sky-300 hover:bg-sky-50 hover:shadow-md">Cancel</button>
          </div>
        </form>
      )}

      <section className="grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
        {vehicleCards}
      </section>
    </div>
  );
}

export default AdminPage;
