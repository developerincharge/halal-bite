import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { View, Text, ActivityIndicator, StyleSheet } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';

import { AuthProvider, useAuth } from './src/context/AuthContext';
import { CartProvider, useCart } from './src/context/CartContext';

// Screens
import LoginScreen from './src/screens/auth/LoginScreen';
import RegisterScreen from './src/screens/auth/RegisterScreen';
import RestaurantsScreen from './src/screens/restaurants/RestaurantsScreen';
import MenuScreen from './src/screens/restaurants/MenuScreen';
import CartScreen from './src/screens/restaurants/CartScreen';
import CheckoutScreen from './src/screens/restaurants/CheckoutScreen';
import OrdersScreen from './src/screens/orders/OrdersScreen';
import OrderDetailScreen from './src/screens/orders/OrderDetailScreen';

const Stack = createStackNavigator();
const Tab = createBottomTabNavigator();

const styles = StyleSheet.create({
  splash: {
    flex: 1,
    backgroundColor: '#1a1a2e',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 500,
  },
  splashEmoji: {
    fontSize: 48,
    marginBottom: 16,
  },
});

// Bottom tab icon with cart badge
function TabIcon({ emoji, label, focused, badgeCount }: any) {
  return (
    <View style={{ alignItems: 'center' }}>
      <View>
        <Text style={{ fontSize: 22 }}>{emoji}</Text>
        {badgeCount > 0 && (
          <View style={{
            position: 'absolute', top: -4, right: -8,
            backgroundColor: '#f4a261', borderRadius: 10,
            width: 18, height: 18, alignItems: 'center', justifyContent: 'center',
          }}>
            <Text style={{ color: 'white', fontSize: 10, fontWeight: '700' }}>
              {badgeCount}
            </Text>
          </View>
        )}
      </View>
      <Text style={{
        fontSize: 10, marginTop: 2,
        color: focused ? '#f4a261' : '#aaa',
        fontWeight: focused ? '700' : '400',
      }}>
        {label}
      </Text>
    </View>
  );
}

// Restaurant stack (browse → menu → cart → checkout)
function RestaurantStack() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#1a1a2e' },
        headerTintColor: 'white',
        headerTitleStyle: { fontWeight: '700' },
      }}
    >
      <Stack.Screen
        name="Restaurants"
        component={RestaurantsScreen}
        options={{ headerShown: false }}
      />
      <Stack.Screen
        name="Menu"
        component={MenuScreen}
        options={({ route }: any) => ({ title: route.params?.restaurantName ?? 'Menu' })}
      />
      <Stack.Screen name="Cart" component={CartScreen} options={{ title: 'Your Cart' }} />
      <Stack.Screen name="Checkout" component={CheckoutScreen} options={{ title: 'Checkout' }} />
    </Stack.Navigator>
  );
}

// Orders stack (list → detail)
function OrdersStack() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#1a1a2e' },
        headerTintColor: 'white',
        headerTitleStyle: { fontWeight: '700' },
      }}
    >
      <Stack.Screen
        name="OrdersList"
        component={OrdersScreen}
        options={{ headerShown: false }}
      />
      <Stack.Screen
        name="OrderDetail"
        component={OrderDetailScreen}
        options={{ title: 'Order Details' }}
      />
    </Stack.Navigator>
  );
}

// Main tab navigator — shown when logged in
function MainTabs() {
  const { totalItems } = useCart();

  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          backgroundColor: '#1a1a2e',
          borderTopColor: 'rgba(255,255,255,0.1)',
          height: 70, paddingBottom: 10,
        },
        tabBarShowLabel: false,
      }}
    >
      <Tab.Screen
        name="Home"
        component={RestaurantStack}
        options={{
          tabBarIcon: ({ focused }) => (
            <TabIcon emoji="🏠" label="Restaurants" focused={focused} badgeCount={totalItems} />
          ),
        }}
      />
      <Tab.Screen
        name="Orders"
        component={OrdersStack}
        options={{
          tabBarIcon: ({ focused }) => (
            <TabIcon emoji="🧾" label="Orders" focused={focused} badgeCount={0} />
          ),
        }}
      />
    </Tab.Navigator>
  );
}

// Auth stack — shown when not logged in
function AuthStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="Register" component={RegisterScreen} />
    </Stack.Navigator>
  );
}

// Root navigator — switches between auth and main based on login state
function RootNavigator() {
  const { isLoggedIn, isLoading } = useAuth();

if (isLoading) {
  return (
    <View style={styles.splash}>
      <Text style={styles.splashEmoji}>🥙</Text>
      <ActivityIndicator color="#f4a261" size="large" />
    </View>
  );
}

  return isLoggedIn ? <MainTabs /> : <AuthStack />;
}

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <AuthProvider>
          <CartProvider>
            <NavigationContainer>
              <RootNavigator />
            </NavigationContainer>
          </CartProvider>
        </AuthProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
