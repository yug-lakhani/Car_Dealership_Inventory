import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";

function Navbar() {
  const { isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="flex items-center justify-between bg-slate-900 px-6 py-4 text-white shadow-md">
      <Link to="/" className="text-lg font-semibold tracking-tight">
        Car Dealership Inventory
      </Link>
      <div className="flex items-center gap-4 text-sm">
        {isAuthenticated ? (
          <>
            <Link to="/" className="hover:text-sky-400">
              Dashboard
            </Link>
            {isAdmin && (
              <Link to="/admin" className="hover:text-sky-400">
                Admin
              </Link>
            )}
            <button
              onClick={handleLogout}
              className="rounded bg-slate-700 px-3 py-1.5 hover:bg-slate-600"
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="hover:text-sky-400">
              Login
            </Link>
            <Link to="/register" className="hover:text-sky-400">
              Register
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
