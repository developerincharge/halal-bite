'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';

const navItems = [
  { href: '/dashboard',    label: 'Dashboard',    icon: '📊' },
  { href: '/restaurants',  label: 'Restaurants',  icon: '🏪' },
  { href: '/orders',       label: 'Orders',       icon: '🧾' },
];

export default function Sidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  return (
    <aside className="w-60 bg-[#1a1a2e] min-h-screen flex flex-col flex-shrink-0">
      {/* Logo */}
      <div className="px-6 py-6 border-b border-white/10">
        <div className="text-3xl mb-1">🥙</div>
        <div className="text-[#f4a261] font-bold text-lg tracking-wide">Halal Bite</div>
        <div className="text-white/40 text-xs uppercase tracking-widest mt-0.5">Admin Portal</div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {navItems.map((item) => {
          const active = pathname.startsWith(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm transition-all ${
                active
                  ? 'bg-[#f4a261] text-white font-semibold'
                  : 'text-white/60 hover:bg-white/10 hover:text-white'
              }`}
            >
              <span className="text-lg">{item.icon}</span>
              {item.label}
            </Link>
          );
        })}
      </nav>

      {/* User + Logout */}
      <div className="px-4 py-4 border-t border-white/10">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-9 h-9 rounded-full bg-[#f4a261] flex items-center justify-center font-bold text-white text-sm flex-shrink-0">
            {user?.email?.charAt(0).toUpperCase()}
          </div>
          <div className="min-w-0">
            <div className="text-white/80 text-xs truncate">{user?.email}</div>
            <div className="text-white/40 text-xs uppercase tracking-wider">Admin</div>
          </div>
        </div>
        <button
          onClick={logout}
          className="w-full py-2 px-3 text-xs text-white/50 hover:text-red-400 hover:bg-white/5 rounded-lg transition-all text-left"
        >
          Sign Out
        </button>
      </div>
    </aside>
  );
}
