# Halal Bite — Customer App (React Native / Expo)

Mobile ordering app for customers to browse restaurants, order food, and track deliveries.

## Prerequisites

- Node.js 18+
- Expo CLI: `npm install -g expo-cli`
- For iOS: Xcode + iOS Simulator (Mac only)
- For Android: Android Studio + Android Emulator
- OR: Expo Go app on your physical phone (easiest!)

## Setup

```bash
cd frontend/customer-app
npm install
npm start
```

This opens the Expo dev server. Then:

- **Physical phone** → Install Expo Go from App Store/Play Store → scan the QR code
- **iOS Simulator** → press `i` in the terminal
- **Android Emulator** → press `a` in the terminal
- **Web browser** → press `w` in the terminal (limited functionality)

## API Configuration

Edit `src/services/api.ts` to change the backend URL:

```typescript
// For Android emulator — use 10.0.2.2 instead of localhost:
const BASE_URL = 'http://10.0.2.2:8080/api/v1';

// For iOS simulator or web:
const BASE_URL = 'http://localhost:8080/api/v1';

// For physical device — use your machine's local IP:
const BASE_URL = 'http://192.168.1.xxx:8080/api/v1';
```

## Screens

| Screen | Description |
|---|---|
| Login | Sign in with email/password |
| Register | Create a customer account |
| Restaurants | Browse and search all active restaurants |
| Menu | View restaurant menu by category, add to cart |
| Cart | Review cart, adjust quantities |
| Checkout | Enter delivery address, place order |
| Orders | View order history |
| Order Detail | Track order status live (polls every 15s) |

## Full Test Flow

1. Register a new customer account
2. Browse restaurants — your restaurant from the dashboard should appear
3. Tap a restaurant → view menu categories and items
4. Add items to cart → tap the cart button
5. Adjust quantities → tap Checkout
6. Enter delivery address → Place Order
7. Switch to the restaurant dashboard → new order appears!
8. Advance order status on dashboard → watch the customer app update
