'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { restaurantApi } from '@/lib/api';
import { Restaurant } from '@/types';
import StatusBadge from '@/components/StatusBadge';

export default function DashboardPage() {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const { data } = await restaurantApi.getPending();
      setRestaurants(data.content ?? []);
    } catch (e) {
      console.error('Failed to load restaurants', e);
    } finally {
      setLoading(false);
    }
  };

  const pending = restaurants.filter(r => r.status === 'PENDING');
  const active  = restaurants.filter(r => r.status === 'ACTIVE');
  const suspended = restaurants.filter(r => r.status === 'SUSPENDED');

  const stats = [
    { label: 'Total Restaurants', value: restaurants.length, icon: '🏪', color: 'bg-blue-50 text-blue-600' },
    { label: 'Active',            value: active.length,      icon: '✅', color: 'bg-green-50 text-green-600' },
    { label: 'Pending Approval',  value: pending.length,     icon: '⏳', color: 'bg-yellow-50 text-yellow-600' },
    { label: 'Suspended',         value: suspended.length,   icon: '🚫', color: 'bg-red-50 text-red-600' },
  ];

  return (
    <div className="p-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-500 text-sm mt-1">Platform overview and quick actions</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-4 gap-4 mb-8">
        {stats.map((stat) => (
          <div key={stat.label} className="card">
            <div className={`inline-flex items-center justify-center w-10 h-10 rounded-xl text-xl mb-3 ${stat.color}`}>
              {stat.icon}
            </div>
            <div className="text-3xl font-bold text-gray-900">{stat.value}</div>
            <div className="text-sm text-gray-500 mt-0.5">{stat.label}</div>
          </div>
        ))}
      </div>

      {/* Pending Restaurants */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-bold text-gray-900">
            Pending Approval
            {pending.length > 0 && (
              <span className="ml-2 bg-yellow-100 text-yellow-800 text-xs font-semibold px-2 py-0.5 rounded-full">
                {pending.length}
              </span>
            )}
          </h2>
          <Link href="/restaurants" className="text-sm text-[#f4a261] hover:underline font-medium">
            View all →
          </Link>
        </div>

        {loading ? (
          <div className="text-center py-8 text-gray-400">Loading...</div>
        ) : pending.length === 0 ? (
          <div className="text-center py-8">
            <div className="text-3xl mb-2">🎉</div>
            <p className="text-gray-500 text-sm">No restaurants pending approval</p>
          </div>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50">
                <th className="table-th">Restaurant</th>
                <th className="table-th">Cuisine</th>
                <th className="table-th">City</th>
                <th className="table-th">Registered</th>
                <th className="table-th">Action</th>
              </tr>
            </thead>
            <tbody>
              {pending.map((r) => (
                <tr key={r.id} className="table-row">
                  <td className="table-td font-semibold">{r.name}</td>
                  <td className="table-td text-[#f4a261]">{r.cuisineType}</td>
                  <td className="table-td">{r.city}</td>
                  <td className="table-td text-gray-400">
                    {new Date(r.createdAt).toLocaleDateString()}
                  </td>
                  <td className="table-td">
                    <Link
                      href={`/restaurants/${r.id}`}
                      className="text-[#f4a261] hover:underline text-sm font-medium"
                    >
                      Review →
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
