'use client';

import { useEffect, useState } from 'react';
import { restaurantApi, orderApi } from '@/lib/api';
import { Restaurant, Order } from '@/types';
import StatusBadge from '@/components/StatusBadge';

export default function OrdersPage() {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedRestaurant, setSelectedRestaurant] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [loadingRestaurants, setLoadingRestaurants] = useState(true);

  useEffect(() => {
    loadRestaurants();
  }, []);

  useEffect(() => {
    if (selectedRestaurant) loadOrders(selectedRestaurant);
  }, [selectedRestaurant]);

  const loadRestaurants = async () => {
    try {
      const { data } = await restaurantApi.getPending();
      const active = (data.content ?? []).filter(
        (r: Restaurant) => r.status === 'ACTIVE'
      );
      setRestaurants(active);
      if (active.length > 0) setSelectedRestaurant(active[0].id);
    } catch (e) {
      console.error(e);
    } finally {
      setLoadingRestaurants(false);
    }
  };

  const loadOrders = async (restaurantId: string) => {
    setLoading(true);
    try {
      const { data } = await orderApi.getByRestaurant(restaurantId);
      setOrders(data.content ?? []);
    } catch (e) {
      console.error(e);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleString('en-US', {
      month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Orders</h1>
        <p className="text-gray-500 text-sm mt-0.5">
          View orders across all active restaurants
        </p>
      </div>

      {/* Restaurant selector */}
      <div className="card mb-6 p-4">
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          Select Restaurant
        </label>
        {loadingRestaurants ? (
          <div className="text-gray-400 text-sm">Loading restaurants...</div>
        ) : restaurants.length === 0 ? (
          <div className="text-gray-400 text-sm">
            No active restaurants yet. Approve restaurants first.
          </div>
        ) : (
          <select
            value={selectedRestaurant}
            onChange={(e) => setSelectedRestaurant(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#f4a261] w-80"
          >
            {restaurants.map((r) => (
              <option key={r.id} value={r.id}>
                {r.name} — {r.city}
              </option>
            ))}
          </select>
        )}
      </div>

      {/* Orders table */}
      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="text-center py-12 text-gray-400">
            Loading orders...
          </div>
        ) : orders.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-3xl mb-2">🧾</div>
            <p className="text-gray-500 text-sm">No orders for this restaurant yet</p>
          </div>
        ) : (
          <>
            <div className="px-6 py-3 bg-gray-50 border-b border-gray-100 text-sm text-gray-500">
              {orders.length} order{orders.length !== 1 ? 's' : ''}
            </div>
            <table className="w-full">
              <thead>
                <tr className="bg-gray-50">
                  <th className="table-th">Order ID</th>
                  <th className="table-th">Customer</th>
                  <th className="table-th">Items</th>
                  <th className="table-th">Total</th>
                  <th className="table-th">Status</th>
                  <th className="table-th">Date</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id} className="table-row">
                    <td className="table-td font-mono text-xs text-gray-500">
                      #{order.id.slice(-8).toUpperCase()}
                    </td>
                    <td className="table-td font-mono text-xs text-gray-500">
                      {order.customerId?.slice(-8).toUpperCase() ?? '—'}
                    </td>
                    <td className="table-td">
                      {order.lineItems?.length ?? 0} item(s)
                    </td>
                    <td className="table-td font-semibold text-gray-900">
                      ${Number(order.totalAmount).toFixed(2)}
                    </td>
                    <td className="table-td">
                      <StatusBadge status={order.status} />
                    </td>
                    <td className="table-td text-gray-400 text-xs">
                      {formatDate(order.createdAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        )}
      </div>
    </div>
  );
}
