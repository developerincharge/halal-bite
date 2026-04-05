import React, { useEffect, useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet,
  ActivityIndicator, TouchableOpacity,
} from 'react-native';
import { orderApi, Order, OrderStatus } from '../../services/api';

const STATUS_STEPS: { status: OrderStatus; label: string; icon: string }[] = [
  { status: 'PENDING',   label: 'Order Received',    icon: '📥' },
  { status: 'CONFIRMED', label: 'Payment Confirmed',  icon: '✅' },
  { status: 'PREPARING', label: 'Being Prepared',     icon: '👨‍🍳' },
  { status: 'READY',     label: 'Ready!',             icon: '📦' },
  { status: 'DELIVERED', label: 'Delivered',          icon: '🎉' },
];

export default function OrderDetailScreen({ route, navigation }: any) {
  const { orderId } = route.params;
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOrder();
    // Poll for status updates every 15 seconds while active orders are open
    const interval = setInterval(loadOrder, 15000);
    return () => clearInterval(interval);
  }, []);

  const loadOrder = async () => {
    try {
      const { data } = await orderApi.getOrderById(orderId);
      setOrder(data);
    } catch (e) {
      console.error('Failed to load order', e);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#f4a261" />
      </View>
    );
  }

  if (!order) {
    return (
      <View style={styles.center}>
        <Text style={styles.errorText}>Order not found</Text>
      </View>
    );
  }

  const currentStepIdx = STATUS_STEPS.findIndex(s => s.status === order.status);
  const isCancelled = order.status === 'CANCELLED';

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.scroll}>

      {/* Order ID + Status */}
      <View style={styles.header}>
        <Text style={styles.orderId}>
          Order #{order.id.slice(-8).toUpperCase()}
        </Text>
        <Text style={styles.orderDate}>
          {new Date(order.createdAt).toLocaleString()}
        </Text>
      </View>

      {/* Status tracker */}
      <View style={styles.card}>
        <Text style={styles.cardTitle}>Order Status</Text>

        {isCancelled ? (
          <View style={styles.cancelledBox}>
            <Text style={styles.cancelledText}>❌ Order Cancelled</Text>
          </View>
        ) : (
          <View style={styles.tracker}>
            {STATUS_STEPS.map((step, idx) => {
              const isCompleted = idx < currentStepIdx;
              const isCurrent = idx === currentStepIdx;
              return (
                <View key={step.status} style={styles.trackerStep}>
                  <View style={[
                    styles.stepDot,
                    isCompleted && styles.stepDotDone,
                    isCurrent && styles.stepDotCurrent,
                  ]}>
                    <Text style={styles.stepIcon}>{step.icon}</Text>
                  </View>
                  {idx < STATUS_STEPS.length - 1 && (
                    <View style={[
                      styles.stepLine,
                      isCompleted && styles.stepLineDone,
                    ]} />
                  )}
                  <Text style={[
                    styles.stepLabel,
                    isCurrent && styles.stepLabelCurrent,
                    isCompleted && styles.stepLabelDone,
                  ]}>
                    {step.label}
                  </Text>
                </View>
              );
            })}
          </View>
        )}

        {order.estimatedReadyAt && order.status !== 'DELIVERED' && !isCancelled && (
          <Text style={styles.eta}>
            🕐 Estimated ready: {new Date(order.estimatedReadyAt).toLocaleTimeString([], {
              hour: '2-digit', minute: '2-digit'
            })}
          </Text>
        )}
      </View>

      {/* Delivery address */}
      <View style={styles.card}>
        <Text style={styles.cardTitle}>📍 Delivery Address</Text>
        <Text style={styles.address}>{order.deliveryStreetAddress}</Text>
        <Text style={styles.address}>{order.deliveryCity}{order.deliveryState ? `, ${order.deliveryState}` : ''} {order.deliveryPostalCode}</Text>
      </View>

      {/* Items ordered */}
      <View style={styles.card}>
        <Text style={styles.cardTitle}>🧾 Items Ordered</Text>
        {order.lineItems.map(item => (
          <View key={item.id} style={styles.lineItem}>
            <Text style={styles.lineItemName}>
              {item.quantity}× {item.itemName}
            </Text>
            <Text style={styles.lineItemPrice}>
              ${item.lineTotal.toFixed(2)}
            </Text>
          </View>
        ))}

        <View style={styles.divider} />

        <View style={styles.lineItem}>
          <Text style={styles.subtotalLabel}>Subtotal</Text>
          <Text style={styles.subtotalValue}>${order.subtotal.toFixed(2)}</Text>
        </View>
        <View style={styles.lineItem}>
          <Text style={styles.subtotalLabel}>Delivery Fee</Text>
          <Text style={styles.subtotalValue}>${order.deliveryFee.toFixed(2)}</Text>
        </View>
        <View style={[styles.lineItem, styles.totalRow]}>
          <Text style={styles.totalLabel}>Total</Text>
          <Text style={styles.totalAmount}>${order.totalAmount.toFixed(2)}</Text>
        </View>
      </View>

      <TouchableOpacity
        style={styles.refreshBtn}
        onPress={loadOrder}
      >
        <Text style={styles.refreshBtnText}>🔄 Refresh Status</Text>
      </TouchableOpacity>

    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f8f9fa' },
  scroll: { padding: 16, paddingBottom: 32 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  errorText: { color: '#888', fontSize: 16 },
  header: { marginBottom: 16 },
  orderId: { fontSize: 20, fontWeight: '800', color: '#1a1a2e' },
  orderDate: { fontSize: 13, color: '#888', marginTop: 4 },
  card: {
    backgroundColor: 'white', borderRadius: 14,
    padding: 16, marginBottom: 14,
  },
  cardTitle: { fontSize: 14, fontWeight: '700', color: '#1a1a2e', marginBottom: 14 },
  tracker: { gap: 0 },
  trackerStep: { flexDirection: 'row', alignItems: 'center', marginBottom: 0 },
  stepDot: {
    width: 36, height: 36, borderRadius: 18,
    backgroundColor: '#f0f0f0', alignItems: 'center',
    justifyContent: 'center', zIndex: 1,
  },
  stepDotDone: { backgroundColor: '#d4edda' },
  stepDotCurrent: { backgroundColor: '#f4a261' },
  stepLine: {
    position: 'absolute', left: 17, top: 36,
    width: 2, height: 28, backgroundColor: '#eee',
  },
  stepLineDone: { backgroundColor: '#28a745' },
  stepIcon: { fontSize: 16 },
  stepLabel: {
    fontSize: 13, color: '#aaa', marginLeft: 12,
    paddingBottom: 28,
  },
  stepLabelCurrent: { color: '#1a1a2e', fontWeight: '700' },
  stepLabelDone: { color: '#28a745' },
  cancelledBox: {
    backgroundColor: '#f8d7da', borderRadius: 10,
    padding: 14, alignItems: 'center',
  },
  cancelledText: { color: '#721c24', fontWeight: '700', fontSize: 15 },
  eta: { fontSize: 13, color: '#f4a261', fontWeight: '600', marginTop: 12 },
  address: { fontSize: 14, color: '#555', marginBottom: 4 },
  lineItem: {
    flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8,
  },
  lineItemName: { fontSize: 14, color: '#333', flex: 1 },
  lineItemPrice: { fontSize: 14, fontWeight: '600', color: '#333' },
  divider: { height: 1, backgroundColor: '#eee', marginVertical: 10 },
  subtotalLabel: { fontSize: 13, color: '#888' },
  subtotalValue: { fontSize: 13, fontWeight: '600', color: '#555' },
  totalRow: { paddingTop: 8, borderTopWidth: 1, borderTopColor: '#eee', marginTop: 4 },
  totalLabel: { fontSize: 16, fontWeight: '700', color: '#1a1a2e' },
  totalAmount: { fontSize: 18, fontWeight: '800', color: '#1a1a2e' },
  refreshBtn: {
    backgroundColor: 'white', borderRadius: 12, borderWidth: 1.5,
    borderColor: '#f4a261', padding: 14, alignItems: 'center',
  },
  refreshBtnText: { color: '#f4a261', fontWeight: '700', fontSize: 14 },
});
