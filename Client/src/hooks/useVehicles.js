import { useCallback, useEffect, useState } from "react";
import { getVehicles, searchVehicles } from "../api/vehicleApi";

/**
 * Loads the vehicle inventory and exposes a `search` function to
 * re-fetch a filtered subset from the backend. Supports server-side
 * pagination (Spring Page) and exposes pagination controls.
 */
export function useVehicles() {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(9);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchAll = useCallback(
    async (opts = {}) => {
      setLoading(true);
      setError(null);
      try {
        const params = { page, size, ...opts };
        const { data } = await getVehicles(params);
        const content = Array.isArray(data) ? data : data?.content || [];
        setVehicles(content);
        setPage(data?.number ?? params.page ?? 0);
        setSize(data?.size ?? params.size ?? size);
        setTotalPages(data?.totalPages ?? 0);
        setTotalElements(data?.totalElements ?? 0);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    },
    [page, size]
  );

  const search = useCallback(
    async (filters = {}, opts = {}) => {
      setLoading(true);
      setError(null);
      try {
        const params = { page, size, ...opts, ...filters };
        const { data } = await searchVehicles(params);
        const content = Array.isArray(data) ? data : data?.content || [];
        setVehicles(content);
        setPage(data?.number ?? params.page ?? 0);
        setSize(data?.size ?? params.size ?? size);
        setTotalPages(data?.totalPages ?? 0);
        setTotalElements(data?.totalElements ?? 0);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    },
    [page, size]
  );

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  const goToPage = (p) => {
    setPage(p);
    fetchAll({ page: p });
  };

  const setPageSize = (s) => {
    setSize(s);
    setPage(0);
    fetchAll({ page: 0, size: s });
  };

  return {
    vehicles,
    loading,
    error,
    refetch: fetchAll,
    search,
    page,
    size,
    totalPages,
    totalElements,
    goToPage,
    setPageSize,
  };
}
