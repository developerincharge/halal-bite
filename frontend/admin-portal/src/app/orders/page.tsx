'use client';

import { useEffect, useState } from 'react';
import { restaurantApi, orderApi } from '@/lib/api';
import { Restaurant, Order } from '@/types';
import StatusBadge from '@/components/StatusBadge';
import api from '@/lib/api';

export default function OrdersPage() {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [payments, setPayments] = useState<Record<string, any>>({});
  const [selectedRestaurant, setSelectedRestaurant] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingRestaurants, setLoadingRestaurants] = useState(true);
  const [copiedId, setCopiedId] = useState('');

  useEffect(() => { loadRestaurants(); }, []);
  useEffect(() => { if (selectedRestaurant) loadOrders(selectedRestaurant); }, [selectedRestaurant]);

  const loadRestaurants = async () => {
    try {
      const { data } = await restaurantApi.getPending();
      const all = data.content ?? [];
      setRestaurants(all);
      if (all.length > 0) setSelectedRestaurant(all[0].id);
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
      const orderList = data.content ?? [];
      setOrders(orderList);
      // Fetch payment info for each order
      const paymentMap: Record<string, any> = {};
      await Promise.all(
        orderList.map(async (order: Order) => {
          try {
            const { data: payment } = await api.get(
              `/payments/order/${order.id}`
            );
            paymentMap[order.id] = payment;
          } catch {
            paymentMap[order.id] = null;
          }
        })
      );
      setPayments(paymentMap);
    } catch (e) {
      console.error(e);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(''), 2000);
  };

  const openApprovalUrl = (url: string) => {
    window.open(url, '_blank');
  };

  const formatDate = (val: any) => {
    if (!val) return '—';
    if (Array.isArray(val)) {
      const [y, m, d] = val;
      return new Date(y, m - 1, d).toLocaleDateString();
    }
    const d = new Date(val);
    return isNaN(d.getTime()) ? '—' : d.toLocaleString();
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Orders</h1>
        <p className="text-gray-500 text-sm mt-0.5">
          View orders and PayPal payment links
        </p>
      </div>

      {/* Restaurant selector */}
      <div className="card mb-6 p-4">
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          Select Restaurant
        </label>
        {loadingRestaurants ? (
          <div className="text-gray-400 text-sm">Loading...</div>
        ) : restaurants.length === 0 ? (
          <div className="text-gray-400 text-sm">No restaurants yet</div>
        ) : (
          <select
            value={selectedRestaurant}
            onChange={(e) => setSelectedRestaurant(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#f4a261] w-80"
          >
            {restaurants.map((r) => (
              <option key={r.id} value={r.id}>
                {r.name} — {r.status}
              </option>
            ))}
          </select>
        )}
      </div>

      {/* Orders table */}
      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="text-center py-12 text-gray-400">Loading orders...</div>
        ) : orders.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-3xl mb-2">🧾</div>
            <p className="text-gray-500 text-sm">No orders yet</p>
          </div>
        ) : (
          <>
            <div className="px-6 py-3 bg-gray-50 border-b text-sm text-gray-500">
              {orders.length} order(s)
            </div>
            <table className="w-full">
              <thead>
                <tr className="bg-gray-50">
                  <th className="table-th">Order ID</th>
                  <th className="table-th">Total</th>
                  <th className="table-th">Order Status</th>
                  <th className="table-th">Payment Status</th>
                  <th className="table-th">PayPal Approval</th>
                  <th className="table-th">Date</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => {
                  const payment = payments[order.id];
                  return (
                    <tr key={order.id} className="table-row">
                      <td className="table-td font-mono text-xs">
                        #{order.id.slice(-8).toUpperCase()}
                      </td>
                      <td className="table-td font-semibold">
                        ${Number(order.totalAmount).toFixed(2)}
                      </td>
                      <td className="table-td">
                        <StatusBadge status={order.status} />
                      </td>
                      <td className="table-td">
                        {payment ? (
                          <StatusBadge status={payment.status} />
                        ) : (
                          <span className="text-gray-400 text-xs">No payment</span>
                        )}
                      </td>
                      <td className="table-td">
                        {payment?.approvalUrl ? (
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => openApprovalUrl(payment.approvalUrl)}
                              className="text-xs bg-[#f4a261] text-white px-3 py-1.5 rounded-lg font-semibold hover:bg-[#e76f51] transition-colors"
                            >
                              Open PayPal →
                            </button>
                            <button
                              onClick={() => copyToClipboard(payment.approvalUrl, order.id)}
                              className="text-xs bg-gray-100 text-gray-600 px-3 py-1.5 rounded-lg font-semibold hover:bg-gray-200 transition-colors"
                            >
                              {copiedId === order.id ? '✅ Copied' : 'Copy URL'}
                            </button>
                          </div>
                        ) : payment?.status === 'SUCCEEDED' ? (
                          <span className="text-green-600 text-xs font-semibold">
                            ✅ Paid
                          </span>
                        ) : payment?.status === 'FAILED' ? (
                          <span className="text-red-500 text-xs">
                            ❌ {payment.failureReason ?? 'Failed'}
                          </span>
                        ) : (
                          <span className="text-gray-400 text-xs">—</span>
                        )}
                      </td>
                      <td className="table-td text-xs text-gray-400">
                        {formatDate(order.createdAt)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </>
        )}
      </div>
    </div>
  );
}