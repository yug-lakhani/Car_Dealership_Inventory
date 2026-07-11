import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { login as loginApi } from "../api/authApi.js";
import { useAuth } from "../context/AuthContext.jsx";

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const successMessage = location.state?.successMessage ?? "";
  const from = location.state?.from?.pathname || "/";

  useEffect(() => {
    if (isAuthenticated) {
      navigate(from, { replace: true });
    }
  }, [isAuthenticated, navigate, from]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (!email.trim() || !password.trim()) {
      setError("Please enter both email and password.");
      return;
    }

    setLoading(true);
    try {
      const response = await loginApi({ email, password });
      const userData = {
        id: response.data.id,
        username: response.data.username,
        email: response.data.email,
        role: response.data.role,
      };
      login(userData, response.data.token);
      navigate(from, { replace: true });
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          "Unable to login. Please check your credentials and try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-96px)] items-center justify-center py-10">
      <div className="mx-auto grid w-full max-w-6xl gap-8 rounded-3xl bg-white/90 shadow-2xl ring-1 ring-slate-200 backdrop-blur-lg md:grid-cols-[1.2fr_1fr]">
        <div className="hidden flex-col justify-center gap-6 rounded-l-3xl bg-slate-950 p-10 text-white md:flex">
          <div>
            <p className="text-sm uppercase tracking-[0.35em] text-sky-300">Car Dealership Inventory</p>
            <h1 className="mt-4 text-4xl font-semibold leading-tight">
              Bring your showroom online with confidence.
            </h1>
          </div>
          <div className="space-y-4 text-slate-300">
            <p className="text-base leading-relaxed">
              Sign in to manage inventory, track vehicles, and control stock from one elegant dashboard.
            </p>
            <div className="grid gap-3 text-sm text-slate-300">
              <div className="rounded-2xl bg-white/5 p-4">
                <p className="font-semibold text-slate-100">Fast inventory access</p>
                <p>Review vehicles and stock levels instantly.</p>
              </div>
              <div className="rounded-2xl bg-white/5 p-4">
                <p className="font-semibold text-slate-100">Secure authentication</p>
                <p>JWT-backed login keeps your admin and staff data safe.</p>
              </div>
            </div>
          </div>
        </div>

        <div className="px-8 py-10 sm:px-12">
          <div className="mb-8">
            <p className="text-sm font-semibold text-sky-600">Welcome back</p>
            <h2 className="mt-3 text-3xl font-bold text-slate-900">Login to your account</h2>
            <p className="mt-2 text-sm text-slate-500">
              Enter your registered email and password to continue.
            </p>
          </div>

          {successMessage && (
            <div className="mb-4 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900">
              {successMessage}
            </div>
          )}

          {error && (
            <div className="mb-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-900">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <label className="block">
              <span className="text-sm font-medium text-slate-700">Email</span>
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                placeholder="you@example.com"
                autoComplete="email"
              />
            </label>

            <label className="block">
              <span className="text-sm font-medium text-slate-700">Password</span>
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                placeholder="********"
                autoComplete="current-password"
              />
            </label>

            <button
              type="submit"
              disabled={loading}
              className="flex w-full items-center justify-center rounded-2xl bg-sky-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-sky-700 disabled:cursor-not-allowed disabled:bg-slate-400"
            >
              {loading ? "Signing in..." : "Sign In"}
            </button>
          </form>

          <div className="mt-6 border-t border-slate-200 pt-5 text-sm text-slate-600">
            Don’t have an account?{' '}
            <Link to="/register" className="font-semibold text-sky-600 hover:text-sky-700">
              Create one now
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
