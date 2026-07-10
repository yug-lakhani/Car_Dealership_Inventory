import { useCallback, useEffect, useState } from "react";
import { getVehicles, searchVehicles } from "../api/vehicleApi";

/**
 * Loads the vehicle inventory and exposes a `search` function to
 * re-fetch a filtered subset from the backend.
 */
export function useVehicles() {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await getVehicles();
      setVehicles(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  const search = useCallback(async (filters) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await searchVehicles(filters);
      setVehicles(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  return { vehicles, loading, error, refetch: fetchAll, search };
}
