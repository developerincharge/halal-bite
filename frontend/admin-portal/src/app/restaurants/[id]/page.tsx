'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { restaurantApi } from '@/lib/api';
import { Restaurant } from '@/types';
import StatusBadge from '@/components/StatusBadge';

export default function RestaurantDetailPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => { loadRestaurant(); }, []);

  const loadRestaurant = async () => {
    try {
      const { data } = await restaurantApi.getById(params.id);
      setRestaurant(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (status: string) => {
    setUpdating(true);
    setMessage('');
    try {
      const { data } = await restaurantApi.updateStatus(params.id, status);
      setRestaurant(data);
      setMessage(`✅ Restaurant status updated to ${status}`);
    } catch (e) {
      setMessage('❌ Failed to update status');
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-400">Loading...</div>
      </div>
    );
  }

  if (!restaurant) {
    return (
      <div className="p-8 text-center text-gray-400">Restaurant not found</div>
    );
  }

  return (
    <div className="p-8 max-w-4xl">
      {/* Header */}
      <div className="flex items-start justify-between mb-6">
        <div>
          <button
            onClick={() => router.back()}
            className="text-sm text-gray-500 hover:text-gray-700 mb-2 flex items-center gap-1"
          >
            ← Back
          </button>
          <h1 className="text-2xl font-bold text-gray-900">{restaurant.name}</h1>
          <div className="flex items-center gap-3 mt-2">
            <StatusBadge status={restaurant.status} />
            <span className="text-sm text-gray-500">{restaurant.cuisineType}</span>
          </div>
        </div>

        {/* Action buttons */}
        <div className="flex gap-2 flex-wrap justify-end">
          {restaurant.status === 'PENDING' && (
            <>
              <button
                onClick={() => updateStatus('ACTIVE')}
                disabled={updating}
                className="btn-primary disabled:opacity-60"
              >
                ✅ Approve
              </button>
              <button
                onClick={() => updateStatus('SUSPENDED')}
                disabled={updating}
                className="btn-danger disabled:opacity-60"
              >
                🚫 Reject
              </button>
            </>
          )}
          {restaurant.status === 'ACTIVE' && (
            <button
              onClick={() => updateStatus('SUSPENDED')}
              disabled={updating}
              className="btn-danger disabled:opacity-60"
            >
              Suspend Restaurant
            </button>
          )}
          {restaurant.status === 'SUSPENDED' && (
            <button
              onClick={() => updateStatus('ACTIVE')}
              disabled={updating}
              className="btn-primary disabled:opacity-60"
            >
              Reinstate Restaurant
            </button>
          )}
        </div>
      </div>

      {message && (
        <div className={`mb-4 px-4 py-3 rounded-lg text-sm font-medium ${
          message.startsWith('✅')
            ? 'bg-green-50 text-green-700 border border-green-200'
            : 'bg-red-50 text-red-700 border border-red-200'
        }`}>
          {message}
        </div>
      )}

      <div className="grid grid-cols-2 gap-6">

        {/* Restaurant Details */}
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-4">Restaurant Details</h2>
          <dl className="space-y-3">
            {[
              { label: 'Address', value: `${restaurant.streetAddress}, ${restaurant.city}${restaurant.state ? `, ${restaurant.state}` : ''} ${restaurant.postalCode}` },
              { label: 'Phone', value: restaurant.phoneNumber },
              { label: 'Email', value: restaurant.email || '—' },
              { label: 'Halal Certified', value: restaurant.isHalalCertified ? '✅ Yes' : '❌ No' },
              { label: 'Estimated Delivery', value: `${restaurant.estimatedDeliveryMinutes} min` },
              { label: 'Minimum Order', value: `$${restaurant.minimumOrderAmount}` },
            ].map(({ label, value }) => (
              <div key={label} className="flex justify-between py-1 border-b border-gray-50 last:border-0">
                <dt className="text-sm text-gray-500">{label}</dt>
                <dd className="text-sm font-medium text-gray-900">{value}</dd>
              </div>
            ))}
          </dl>
        </div>

        {/* Platform Info */}
        <div className="card">
          <h2 className="font-bold text-gray-900 mb-4">Platform Info</h2>
          <dl className="space-y-3">
            {[
              { label: 'Platform Fee', value: `${(parseFloat(String(restaurant.affiliateRevenuePercentage)) * 100).toFixed(0)}%` },
              { label: 'Average Rating', value: restaurant.totalReviews > 0 ? `⭐ ${restaurant.averageRating?.toFixed(1)}` : 'No reviews yet' },
              { label: 'Total Reviews', value: restaurant.totalReviews?.toString() ?? '0' },
              { label: 'Owner ID', value: restaurant.ownerUserId?.slice(-12) ?? '—' },
              { label: 'Registered', value: new Date(restaurant.createdAt).toLocaleDateString() },
            ].map(({ label, value }) => (
              <div key={label} className="flex justify-between py-1 border-b border-gray-50 last:border-0">
                <dt className="text-sm text-gray-500">{label}</dt>
                <dd className="text-sm font-medium text-gray-900">{value}</dd>
              </div>
            ))}
          </dl>
        </div>

        {/* Description */}
        {restaurant.description && (
          <div className="card col-span-2">
            <h2 className="font-bold text-gray-900 mb-2">Description</h2>
            <p className="text-sm text-gray-600 leading-relaxed">{restaurant.description}</p>
          </div>
        )}
      </div>
    </div>
  );
}
