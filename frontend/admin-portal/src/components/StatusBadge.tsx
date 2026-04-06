'use client';

const configs = {
  // Restaurant statuses
  ACTIVE:    { label: 'Active',    classes: 'bg-green-100 text-green-800' },
  PENDING:   { label: 'Pending',   classes: 'bg-yellow-100 text-yellow-800' },
  SUSPENDED: { label: 'Suspended', classes: 'bg-red-100 text-red-800' },
  CLOSED:    { label: 'Closed',    classes: 'bg-gray-100 text-gray-600' },
  // Order statuses
  CONFIRMED: { label: 'Confirmed', classes: 'bg-blue-100 text-blue-800' },
  PREPARING: { label: 'Preparing', classes: 'bg-indigo-100 text-indigo-800' },
  READY:     { label: 'Ready',     classes: 'bg-cyan-100 text-cyan-800' },
  DELIVERED: { label: 'Delivered', classes: 'bg-gray-100 text-gray-600' },
  CANCELLED: { label: 'Cancelled', classes: 'bg-red-100 text-red-800' },
} as Record<string, { label: string; classes: string }>;

export default function StatusBadge({ status }: { status: string }) {
  const config = configs[status] ?? { label: status, classes: 'bg-gray-100 text-gray-600' };
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${config.classes}`}>
      {config.label}
    </span>
  );
}
