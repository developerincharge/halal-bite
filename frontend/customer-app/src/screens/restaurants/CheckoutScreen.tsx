import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  ScrollView, Alert, ActivityIndicator
} from 'react-native';
import { orderApi } from '../../services/api';
import { useCart } from '../../context/CartContext';

const DELIVERY_FEE = 2.99;

export default function CheckoutScreen({ navigation }: any) {
  const { items, restaurantId, totalAmount, clearCart } = useCart();
  const [streetAddress, setStreetAddress] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [postalCode, setPostalCode] = useState('');
  const [specialInstructions, setSpecialInstructions] = useState('');
  const [loading, setLoading] = useState(false);

  const grandTotal = parseFloat(Number(totalAmount).toFixed(2)) + DELIVERY_FEE;

  const handlePlaceOrder = async () => {
    if (!streetAddress || !city || !postalCode) {
      Alert.alert('Error', 'Please fill in your delivery address');
      return;
    }

    if (!restaurantId) {
      Alert.alert('Error', 'No restaurant selected');
      return;
    }

    setLoading(true);
    try {
      const { data: order } = await orderApi.placeOrder({
        restaurantId,
        items: items.map(i => ({
          menuItemId: i.menuItem.id,
          quantity: i.quantity,
          specialRequests: i.specialRequests,
        })),
        deliveryStreetAddress: streetAddress,
        deliveryCity: city,
        deliveryState: state,
        deliveryPostalCode: postalCode,
        specialInstructions,
      });

      clearCart();

      Alert.alert(
        '🎉 Order Placed!',
        `Your order #${order.id.slice(-8).toUpperCase()} has been placed successfully. You'll receive a confirmation shortly.`,
        [{
          text: 'Track Order',
          onPress: () => navigation.navigate('OrderDetail', { orderId: order.id }),
        }]
      );
    } catch (err: any) {
      const message = err.response?.data?.message ?? 'Failed to place order. Please try again.';
      Alert.alert('Order Failed', message);
    } finally {
      setLoading(false);
    }
  };

return (
  <View style={styles.container}>
    <ScrollView
      contentContainerStyle={styles.scroll}
      keyboardShouldPersistTaps="handled"
    >
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>📍 Delivery Address</Text>
        <Text style={styles.label}>Street Address *</Text>
        <TextInput
          style={styles.input}
          value={streetAddress}
          onChangeText={setStreetAddress}
          placeholder="123 Main Street"
        />
        <View style={styles.row}>
          <View style={styles.rowField}>
            <Text style={styles.label}>City *</Text>
            <TextInput
              style={styles.input}
              value={city}
              onChangeText={setCity}
              placeholder="Chicago"
            />
          </View>
          <View style={styles.rowFieldSmall}>
            <Text style={styles.label}>State</Text>
            <TextInput
              style={styles.input}
              value={state}
              onChangeText={setState}
              placeholder="IL"
              maxLength={2}
              autoCapitalize="characters"
            />
          </View>
        </View>
        <Text style={styles.label}>Postal Code *</Text>
        <TextInput
          style={styles.input}
          value={postalCode}
          onChangeText={setPostalCode}
          placeholder="60601"
          keyboardType="numeric"
          maxLength={10}
        />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>📝 Special Instructions</Text>
        <TextInput
          style={[styles.input, styles.multiline]}
          value={specialInstructions}
          onChangeText={setSpecialInstructions}
          placeholder="Ring the doorbell, leave at the door, etc."
          multiline
          numberOfLines={3}
        />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>🧾 Order Summary</Text>
        {items.map((item) => (
          <View key={item.menuItem.id} style={styles.summaryRow}>
            <Text style={styles.summaryItem}>
              {item.quantity}x {item.menuItem.name}
            </Text>
            <Text style={styles.summaryPrice}>
              ${(parseFloat(String(item.menuItem.discountedPrice ?? item.menuItem.price)) * item.quantity).toFixed(2)}
            </Text>
          </View>
        ))}
        <View style={styles.divider} />
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Subtotal</Text>
          <Text style={styles.summaryPrice}>${Number(totalAmount).toFixed(2)}</Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Delivery Fee</Text>
          <Text style={styles.summaryPrice}>${DELIVERY_FEE.toFixed(2)}</Text>
        </View>
        <View style={[styles.summaryRow, styles.totalRow]}>
          <Text style={styles.totalLabel}>Total</Text>
          <Text style={styles.totalAmount}>${grandTotal.toFixed(2)}</Text>
        </View>
      </View>

      <View style={styles.paymentNote}>
        <Text style={styles.paymentNoteText}>
          💳 Payment will be processed after order confirmation
        </Text>
      </View>

      <TouchableOpacity
        style={[styles.orderBtn, loading && styles.orderBtnDisabled]}
        onPress={handlePlaceOrder}
        disabled={loading}
        activeOpacity={0.8}
      >
        {loading
          ? <ActivityIndicator color="#fff" />
          : <Text style={styles.orderBtnText}>
              Place Order · ${grandTotal.toFixed(2)}
            </Text>
        }
      </TouchableOpacity>

    </ScrollView>
  </View>
);
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f8f9fa' },
  scroll: { padding: 16, paddingBottom: 60 },
  section: {
    backgroundColor: 'white', borderRadius: 14,
    padding: 16, marginBottom: 14,
  },
  sectionTitle: {
    fontSize: 15, fontWeight: '700', color: '#1a1a2e', marginBottom: 14,
  },
  label: { fontSize: 12, fontWeight: '600', color: '#555', marginBottom: 6 },
  input: {
    borderWidth: 1.5, borderColor: '#e0e0e0', borderRadius: 10,
    padding: 12, fontSize: 14, color: '#333', marginBottom: 12,
  },
  multiline: { minHeight: 80, textAlignVertical: 'top' },
  row: { flexDirection: 'row', gap: 10 },
  rowField: { flex: 1 },
  rowFieldSmall: { width: 70 },
  summaryRow: {
    flexDirection: 'row', justifyContent: 'space-between',
    marginBottom: 8,
  },
  summaryItem: { fontSize: 13, color: '#444', flex: 1, marginRight: 8 },
  summaryLabel: { fontSize: 13, color: '#888' },
  summaryPrice: { fontSize: 13, fontWeight: '600', color: '#333' },
  divider: { height: 1, backgroundColor: '#eee', marginVertical: 10 },
  totalRow: { paddingTop: 8, borderTopWidth: 1, borderTopColor: '#eee', marginTop: 2 },
  totalLabel: { fontSize: 16, fontWeight: '700', color: '#1a1a2e' },
  totalAmount: { fontSize: 18, fontWeight: '800', color: '#1a1a2e' },
  paymentNote: {
    backgroundColor: '#fff3e0', borderRadius: 10,
    padding: 12, marginBottom: 16,
  },
  paymentNoteText: { fontSize: 13, color: '#e76f51', textAlign: 'center' },
orderBtn: {
  backgroundColor: '#f4a261',
  borderRadius: 14,
  padding: 20,
  alignItems: 'center',
  marginTop: 8,
  marginBottom: 16,
  minHeight: 56,
},
  orderBtnDisabled: { opacity: 0.6 },
  orderBtnText: { color: 'white', fontSize: 16, fontWeight: '800' },
});
