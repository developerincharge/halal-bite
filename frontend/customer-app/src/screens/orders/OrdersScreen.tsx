import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, RefreshControl,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { orderApi, Order, OrderSummary } from '../../services/api';


const STATUS_CONFIG: Record<string, { label: string; color: string; bg: string }> = {
  PENDING:   { label: 'Pending Payment', color: '#856404', bg: '#fff3cd' },
  CONFIRMED: { label: 'Confirmed',       color: '#004085', bg: '#cce5ff' },
  PREPARING: { label: 'Being Prepared',  color: '#155724', bg: '#d4edda' },
  READY:     { label: 'Ready!',          color: '#0c5460', bg: '#d1ecf1' },
  DELIVERED: { label: 'Delivered',       color: '#383d41', bg: '#e2e3e5' },
  CANCELLED: { label: 'Cancelled',       color: '#721c24', bg: '#f8d7da' },
};

export default function OrdersScreen({ navigation }: any) {

  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  // Reload orders every time this screen comes into focus
  useFocusEffect(
    useCallback(() => {
      loadOrders();
    }, [])
  );

  const loadOrders = async () => {
    try {
      const { data } = await orderApi.getMyOrders();
      setOrders(data.content);
    } catch (e) {
      console.error('Failed to load orders', e);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

const renderOrder = ({ item }: { item: OrderSummary }) => {
  const status = STATUS_CONFIG[item.status] ?? {
    label: item.status, color: '#333', bg: '#eee'
  };

  return (
    <TouchableOpacity
      style={styles.card}
      onPress={() => navigation.navigate('OrderDetail', { orderId: item.id })}
    >
      <View style={styles.cardTop}>
        <Text style={styles.orderId}>
          Order #{item.id.slice(-8).toUpperCase()}
        </Text>
        <View style={[styles.statusBadge, { backgroundColor: status.bg }]}>
          <Text style={[styles.statusText, { color: status.color }]}>
            {status.label}
          </Text>
        </View>
      </View>

      {/* FIXED: use itemCount instead of lineItems.length */}
      <Text style={styles.itemsText}>
        {item.itemCount ?? 0} item(s)
      </Text>

      <View style={styles.cardBottom}>
        <Text style={styles.total}>${item.totalAmount.toFixed(2)}</Text>
        <Text style={styles.date}>
          {new Date(item.createdAt).toLocaleDateString('en-US', {
            month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
          })}
        </Text>
      </View>
    </TouchableOpacity>
  );
};

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#f4a261" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={orders}
        keyExtractor={(item) => item.id}
        renderItem={renderOrder}
        contentContainerStyle={styles.list}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={() => { setRefreshing(true); loadOrders(); }}
            tintColor="#f4a261"
          />
        }
        ListHeaderComponent={
          <Text style={styles.header}>Your Orders</Text>
        }
        ListEmptyComponent={
          <View style={styles.empty}>
            <Text style={styles.emptyIcon}>🧾</Text>
            <Text style={styles.emptyTitle}>No orders yet</Text>
            <Text style={styles.emptySub}>
              Place your first order from a nearby restaurant!
            </Text>
          </View>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f8f9fa' },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  header: {
    fontSize: 22, fontWeight: '800', color: '#1a1a2e',
    paddingHorizontal: 16, paddingTop: 20, paddingBottom: 12,
  },
  list: { paddingHorizontal: 16, paddingBottom: 24 },
  card: {
    backgroundColor: 'white', borderRadius: 14,
    padding: 16, marginBottom: 12,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06, shadowRadius: 8, elevation: 3,
  },
  cardTop: {
    flexDirection: 'row', justifyContent: 'space-between',
    alignItems: 'center', marginBottom: 8,
  },
  orderId: { fontSize: 14, fontWeight: '700', color: '#1a1a2e' },
  statusBadge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20 },
  statusText: { fontSize: 11, fontWeight: '700' },
  itemsText: { fontSize: 13, color: '#888', marginBottom: 10 },
  cardBottom: { flexDirection: 'row', justifyContent: 'space-between' },
  total: { fontSize: 16, fontWeight: '800', color: '#1a1a2e' },
  date: { fontSize: 12, color: '#aaa' },
  empty: { alignItems: 'center', paddingTop: 60 },
  emptyIcon: { fontSize: 56, marginBottom: 12 },
  emptyTitle: { fontSize: 18, fontWeight: '700', color: '#1a1a2e', marginBottom: 8 },
  emptySub: { fontSize: 14, color: '#888', textAlign: 'center', paddingHorizontal: 32 },
});
