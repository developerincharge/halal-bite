'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { restaurantApi } from '@/lib/api';
import { Restaurant, RestaurantStatus } from '@/types';
import StatusBadge from '@/components/StatusBadge';

const tabs: { label: string; status: RestaurantStatus | 'ALL' }[] = [
  { label: 'All',       status: 'ALL' },
  { label: 'Pending',   status: 'PENDING' },
  { label: 'Active',    status: 'ACTIVE' },
  { label: 'Suspended', status: 'SUSPENDED' },
];

export default function RestaurantsPage() {
  const [restaurants, setRestaurants] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<RestaurantStatus | 'ALL'>('ALL');
  const [search, setSearch] = useState('');

  useEffect(() => { loadRestaurants(); }, []);

  const loadRestaurants = async () => {
    try {
      const { data } = await restaurantApi.getPending();
      setRestaurants(data.content ?? []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const filtered = restaurants
    .filter(r => tab === 'ALL' || r.status === tab)
    .filter(r =>
      !search ||
      r.name.toLowerCase().includes(search.toLowerCase()) ||
      r.city.toLowerCase().includes(search.toLowerCase())
    );

  const countByStatus = (status: RestaurantStatus) =>
    restaurants.filter(r => r.status === status).length;

   const formatDate = (dateVal: any): string => {
     if (!dateVal) return '—';
     // Java LocalDateTime comes as array: [2026, 4, 2, 15, 54, 35]
     if (Array.isArray(dateVal)) {
       const [year, month, day] = dateVal;
       return new Date(year, month - 1, day).toLocaleDateString();
     }
     // String format fallback
     const d = new Date(dateVal);
     return isNaN(d.getTime()) ? '—' : d.toLocaleDateString();
   };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Restaurants</h1>
          <p className="text-gray-500 text-sm mt-0.5">{restaurants.length} total</p>
        </div>
        <input
          type="text"
          placeholder="Search by name or city..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#f4a261] w-60"
        />
      </div>

      <div className="flex gap-1 mb-6 border-b border-gray-200">
        {tabs.map((t) => (
          <button
            key={t.status}
            onClick={() => setTab(t.status)}
            className={`px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors ${
              tab === t.status
                ? 'border-[#f4a261] text-[#f4a261]'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            {t.label}
            {t.status !== 'ALL' && (
              <span className="ml-1.5 text-xs bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded-full">
                {restaurants.filter(r => r.status === t.status).length}
              </span>
            )}
          </button>
        ))}
      </div>

           <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
             {loading ? (
               <div className="text-center py-12 text-gray-400">Loading...</div>
             ) : filtered.length === 0 ? (
               <div className="text-center py-12 text-gray-400">No restaurants found</div>
             ) : (
               <table className="w-full">
                 <thead>
                   <tr className="bg-gray-50">
                     <th className="table-th">Restaurant</th>
                     <th className="table-th">Cuisine</th>
                     <th className="table-th">Location</th>
                     <th className="table-th">Status</th>
                     <th className="table-th">Platform Fee</th>
                     <th className="table-th">Registered</th>
                     <th className="table-th"></th>
                   </tr>
                 </thead>
                 <tbody>
                   {filtered.map((r) => (
                     <tr key={r.id} className="border-t border-gray-50 hover:bg-gray-50">
                       <td className="table-td font-semibold">{r.name}</td>
                       <td className="table-td text-[#f4a261]">{r.cuisineType}</td>
                       <td className="table-td">{r.city}</td>
                       <td className="table-td"><StatusBadge status={r.status} /></td>
                       <td className="table-td">
                         {r.affiliateRevenuePercentage
                           ? (parseFloat(r.affiliateRevenuePercentage) * 100).toFixed(0) + '%'
                           : '15%'}
                       </td>
                       <td className="table-td text-gray-400 text-xs">
                         {formatDate(r.createdAt)}
                       </td>
                       <td className="table-td">
                         <Link
                           href={`/restaurants/${r.id}`}
                           className="text-[#f4a261] hover:underline text-sm font-medium"
                         >
                           Manage →
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
