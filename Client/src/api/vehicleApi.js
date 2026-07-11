import axiosClient from "./axiosClient";

export const getVehicles = (params) => axiosClient.get("/vehicles", { params });

export const searchVehicles = (params) => axiosClient.get("/vehicles/search", { params });

export const createVehicle = (payload) => axiosClient.post("/vehicles", payload);

export const updateVehicle = (id, payload) => axiosClient.put(`/vehicles/${id}`, payload);

export const deleteVehicle = (id) => axiosClient.delete(`/vehicles/${id}`);

export const purchaseVehicle = (id, quantity = 1) =>
  axiosClient.post(`/vehicles/${id}/purchase`, { quantity });

export const restockVehicle = (id, quantity) =>
  axiosClient.post(`/vehicles/${id}/restock`, { quantity });
