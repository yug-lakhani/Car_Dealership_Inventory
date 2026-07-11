import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register as registerApi } from "../api/authApi.js";
import { useAuth } from "../context/AuthContext.jsx";

function RegisterPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      navigate("/", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (!username.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()) {
      setError("Please fill in every field before continuing.");
      return;
    }

    if (password.length < 6) {
      setError("Password must be at least 6 characters long.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match. Please confirm your password.");
      return;
    }

    setLoading(true);
    try {
      await registerApi({ username, email, password });
      navigate("/login", {
        replace: true,
        state: { successMessage: "Registration successful! You can now sign in." },
      });
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          "Unable to register. Please review your information and try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-96px)] items-center justify-center py-10">
      <div className="mx-auto grid w-full max-w-6xl gap-8 rounded-3xl bg-white/90 shadow-2xl ring-1 ring-slate-200 backdrop-blur-lg md:grid-cols-[1.2fr_1fr]">
        <div className="hidden flex-col justify-center gap-6 rounded-l-3xl bg-gradient-to-br from-slate-800 via-slate-900 to-slate-950 p-10 text-white md:flex">
          <div>
            <p className="text-sm uppercase tracking-[0.35em] text-sky-300">New Account</p>
            <h1 className="mt-4 text-4xl font-semibold leading-tight">
              Create your dealership management profile.
            </h1>
          </div>
          <div className="space-y-4 text-slate-300">
            <p className="text-base leading-relaxed">
              Register now to start tracking vehicles, managing stock, and growing your showroom with ease.
            </p>
            <div className="grid gap-3 text-sm text-slate-300">
              <div className="rounded-2xl bg-white/5 p-4">
                <p className="font-semibold text-slate-100">Fast onboarding</p>
                <p>Use your email and password to join our inventory system.</p>
              </div>
              <div className="rounded-2xl bg-white/5 p-4">
                <p className="font-semibold text-slate-100">Team-ready access</p>
                <p>Invite staff later from the admin dashboard.</p>
              </div>
            </div>
          </div>
        </div>

        <div className="px-8 py-10 sm:px-12">
          <div className="mb-8">
            <p className="text-sm font-semibold text-sky-600">Join the team</p>
            <h2 className="mt-3 text-3xl font-bold text-slate-900">Register an account</h2>
            <p className="mt-2 text-sm text-slate-500">
              Fill in your details to get started with vehicle inventory management.
            </p>
          </div>

          {error && (
            <div className="mb-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-900">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <label className="block">
              <span className="text-sm font-medium text-slate-700">Username</span>
              <input
                type="text"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                placeholder="Your display name"
                autoComplete="username"
              />
            </label>

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

            <div className="grid gap-5 sm:grid-cols-2">
              <label className="block">
                <span className="text-sm font-medium text-slate-700">Password</span>
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                  placeholder="********"
                  autoComplete="new-password"
                />
              </label>

              <label className="block">
                <span className="text-sm font-medium text-slate-700">Confirm Password</span>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                  placeholder="Repeat password"
                  autoComplete="new-password"
                />
              </label>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="flex w-full items-center justify-center rounded-2xl bg-sky-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-sky-700 disabled:cursor-not-allowed disabled:bg-slate-400"
            >
              {loading ? "Creating account..." : "Create account"}
            </button>
          </form>

          <div className="mt-6 border-t border-slate-200 pt-5 text-sm text-slate-600">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-sky-600 hover:text-sky-700">
              Sign in instead
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default RegisterPage;
