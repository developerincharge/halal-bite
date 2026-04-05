import React, { createContext, useContext, useState } from 'react';
import { CartItem, MenuItem } from '../services/api';

interface CartContextType {
  items: CartItem[];
  restaurantId: string | null;
  restaurantName: string;
  addItem: (menuItem: MenuItem, restaurantId: string, restaurantName: string) => void;
  removeItem: (menuItemId: string) => void;
  updateQuantity: (menuItemId: string, quantity: number) => void;
  clearCart: () => void;
  totalItems: number;
  totalAmount: number;
}

const CartContext = createContext<CartContextType | null>(null);

export function CartProvider({ children }: { children: React.ReactNode }) {
  const [items, setItems] = useState<CartItem[]>([]);
  const [restaurantId, setRestaurantId] = useState<string | null>(null);
  const [restaurantName, setRestaurantName] = useState('');

  /**
   * Add item to cart.
   * If item is from a different restaurant, ask user if they want to clear cart.
   * For simplicity we auto-clear here — production would show a dialog.
   */
  const addItem = (
    menuItem: MenuItem,
    newRestaurantId: string,
    newRestaurantName: string
  ) => {
    // If adding from a different restaurant, clear the cart first
    if (restaurantId && restaurantId !== newRestaurantId) {
      setItems([]);
    }

    setRestaurantId(newRestaurantId);
    setRestaurantName(newRestaurantName);

    setItems(prev => {
      const existing = prev.find(i => i.menuItem.id === menuItem.id);
      if (existing) {
        // Increase quantity if already in cart
        return prev.map(i =>
          i.menuItem.id === menuItem.id
            ? { ...i, quantity: i.quantity + 1 }
            : i
        );
      }
      return [...prev, { menuItem, quantity: 1 }];
    });
  };

  const removeItem = (menuItemId: string) => {
    setItems(prev => prev.filter(i => i.menuItem.id !== menuItemId));
  };

  const updateQuantity = (menuItemId: string, quantity: number) => {
    if (quantity <= 0) {
      removeItem(menuItemId);
      return;
    }
    setItems(prev =>
      prev.map(i =>
        i.menuItem.id === menuItemId ? { ...i, quantity } : i
      )
    );
  };

  const clearCart = () => {
    setItems([]);
    setRestaurantId(null);
    setRestaurantName('');
  };

const totalItems = items.reduce((sum, i) => sum + i.quantity, 0);

const totalAmount = items.reduce((sum, i) => {
  const price = parseFloat(String(i.menuItem.discountedPrice ?? i.menuItem.price));
  const qty = i.quantity;
  return sum + (isNaN(price) ? 0 : price * qty);
}, 0);

  return (
    <CartContext.Provider value={{
      items,
      restaurantId,
      restaurantName,
      addItem,
      removeItem,
      updateQuantity,
      clearCart,
      totalItems,
      totalAmount,
    }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used within CartProvider');
  return ctx;
}
