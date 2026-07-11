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

  return (
    <div className="space-y-6">
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
            className="rounded-2xl bg-sky-600 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-700"
          >
            {showForm ? "Close" : "New vehicle"}
          </button>
        </div>
      </header>

      {msg && <div className="rounded-md bg-emerald-50 px-4 py-2 text-emerald-800">{msg}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="grid gap-4 rounded-2xl border p-6 shadow-sm sm:grid-cols-2">
          <label className="block">
            <span className="text-sm text-slate-700">Make</span>
            <input value={form.make} onChange={(e) => setForm((f) => ({ ...f, make: e.target.value }))} className="mt-2 w-full rounded-xl border px-3 py-2" />
          </label>
          <label className="block">
            <span className="text-sm text-slate-700">Model</span>
            <input value={form.model} onChange={(e) => setForm((f) => ({ ...f, model: e.target.value }))} className="mt-2 w-full rounded-xl border px-3 py-2" />
          </label>
          <label className="block">
            <span className="text-sm text-slate-700">Category</span>
            <input value={form.category} onChange={(e) => setForm((f) => ({ ...f, category: e.target.value }))} className="mt-2 w-full rounded-xl border px-3 py-2" list="cats" />
            <datalist id="cats">{categories.map((c) => <option key={c} value={c} />)}</datalist>
          </label>
          <label className="block">
            <span className="text-sm text-slate-700">Price</span>
            <input type="number" value={form.price} onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))} className="mt-2 w-full rounded-xl border px-3 py-2" />
          </label>
          <label className="block">
            <span className="text-sm text-slate-700">Quantity</span>
            <input type="number" value={form.quantity} onChange={(e) => setForm((f) => ({ ...f, quantity: e.target.value }))} className="mt-2 w-full rounded-xl border px-3 py-2" />
          </label>

          <div className="sm:col-span-2 flex gap-3">
            <button disabled={busy} type="submit" className="rounded-2xl bg-sky-600 px-4 py-2 text-white">{editing ? "Save" : "Create"}</button>
            <button type="button" onClick={() => { resetForm(); setShowForm(false); }} className="rounded-2xl border px-4 py-2">Cancel</button>
          </div>
        </form>
      )}

      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {loading ? (
          <div className="p-6">Loading...</div>
        ) : vehicles.length === 0 ? (
          <div className="p-6">No vehicles</div>
        ) : (
          vehicles.map((v) => (
            <div key={v.id} className="rounded-2xl border p-4 shadow-sm">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-xs uppercase text-sky-600">{v.category}</p>
                  <h3 className="mt-1 text-lg font-semibold">{v.make} {v.model}</h3>
                  <p className="mt-2 text-sm text-slate-600">Price: <span className="font-medium">${Number(v.price).toLocaleString()}</span></p>
                  <p className="text-sm text-slate-600">Qty: <span className="font-medium">{v.quantity}</span></p>
                </div>
                <div className="flex flex-col items-end gap-2">
                  <button onClick={() => startEdit(v)} className="rounded-full bg-slate-100 px-3 py-1 text-sm">Edit</button>
                  <button onClick={() => handleRestock(v.id)} className="rounded-full bg-emerald-50 px-3 py-1 text-sm text-emerald-700">Restock</button>
                  <button onClick={() => handleDelete(v.id)} className="rounded-full bg-rose-50 px-3 py-1 text-sm text-rose-700">Delete</button>
                </div>
              </div>
            </div>
          ))
        )}
      </section>
    </div>
  );
}

export default AdminPage;
