import React from 'react';
import {
  View, Text, FlatList, TouchableOpacity,
  StyleSheet, Alert,
} from 'react-native';
import { useCart } from '../../context/CartContext';

const DELIVERY_FEE = 2.99;

export default function CartScreen({ navigation }: any) {
  const {
    items, restaurantName, totalAmount,
    updateQuantity, removeItem, clearCart,
  } = useCart();

  const grandTotal = Number(totalAmount) + DELIVERY_FEE;

  if (items.length === 0) {
    return (
      <View style={styles.empty}>
        <Text style={styles.emptyIcon}>🛒</Text>
        <Text style={styles.emptyTitle}>Your cart is empty</Text>
        <Text style={styles.emptySub}>Add items from a restaurant to get started</Text>
        <TouchableOpacity
          style={styles.browseBtn}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.browseBtnText}>Browse Restaurants</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.restaurantName}>{restaurantName}</Text>

      <FlatList
        data={items}
        keyExtractor={(item) => item.menuItem.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <View style={styles.cartItem}>
            <View style={styles.itemInfo}>
              <Text style={styles.itemName}>{item.menuItem.name}</Text>
              <Text style={styles.itemPrice}>
                  ${(Number(item.menuItem.discountedPrice ?? item.menuItem.price) * item.quantity).toFixed(2)}
              </Text>
            </View>
            <View style={styles.qtyControl}>
              <TouchableOpacity
                style={styles.qtyBtn}
                onPress={() => updateQuantity(item.menuItem.id, item.quantity - 1)}
              >
                <Text style={styles.qtyBtnText}>−</Text>
              </TouchableOpacity>
              <Text style={styles.qty}>{item.quantity}</Text>
              <TouchableOpacity
                style={styles.qtyBtn}
                onPress={() => updateQuantity(item.menuItem.id, item.quantity + 1)}
              >
                <Text style={styles.qtyBtnText}>+</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}
        ListFooterComponent={
          <View style={styles.summary}>
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Subtotal</Text>
              <Text style={styles.summaryValue}>${totalAmount.toFixed(2)}</Text>
            </View>
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Delivery Fee</Text>
              <Text style={styles.summaryValue}>${DELIVERY_FEE.toFixed(2)}</Text>
            </View>
            <View style={[styles.summaryRow, styles.totalRow]}>
              <Text style={styles.totalLabel}>Total</Text>
              <Text style={styles.totalValue}>${grandTotal.toFixed(2)}</Text>
            </View>
          </View>
        }
      />

      <View style={styles.footer}>
        <TouchableOpacity
          style={styles.clearBtn}
          onPress={() => Alert.alert(
            'Clear Cart',
            'Remove all items?',
            [
              { text: 'Cancel', style: 'cancel' },
              { text: 'Clear', style: 'destructive', onPress: clearCart },
            ]
          )}
        >
          <Text style={styles.clearBtnText}>Clear</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.checkoutBtn}
          onPress={() => navigation.navigate('Checkout')}
        >
          <Text style={styles.checkoutBtnText}>
            Checkout · ${grandTotal.toFixed(2)}
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f8f9fa' },
  restaurantName: {
    fontSize: 16, fontWeight: '700', color: '#1a1a2e',
    padding: 16, paddingBottom: 8,
  },
  list: { paddingHorizontal: 16, paddingBottom: 16 },
  cartItem: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    backgroundColor: 'white', borderRadius: 12, padding: 14, marginBottom: 10,
  },
  itemInfo: { flex: 1 },
  itemName: { fontSize: 15, fontWeight: '600', color: '#1a1a2e' },
  itemPrice: { fontSize: 14, color: '#f4a261', fontWeight: '700', marginTop: 4 },
  qtyControl: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  qtyBtn: {
    width: 32, height: 32, borderRadius: 16,
    backgroundColor: '#f0f0f0', alignItems: 'center', justifyContent: 'center',
  },
  qtyBtnText: { fontSize: 18, fontWeight: '700', color: '#333' },
  qty: { fontSize: 16, fontWeight: '700', minWidth: 20, textAlign: 'center' },
  summary: {
    backgroundColor: 'white', borderRadius: 12,
    padding: 16, marginTop: 8,
  },
  summaryRow: {
    flexDirection: 'row', justifyContent: 'space-between', marginBottom: 10,
  },
  summaryLabel: { fontSize: 14, color: '#666' },
  summaryValue: { fontSize: 14, fontWeight: '600', color: '#333' },
  totalRow: {
    borderTopWidth: 1, borderTopColor: '#eee',
    paddingTop: 12, marginTop: 4, marginBottom: 0,
  },
  totalLabel: { fontSize: 16, fontWeight: '700', color: '#1a1a2e' },
  totalValue: { fontSize: 18, fontWeight: '800', color: '#1a1a2e' },
  footer: {
    flexDirection: 'row', padding: 16, gap: 12,
    backgroundColor: 'white', borderTopWidth: 1, borderTopColor: '#eee',
  },
  clearBtn: {
    paddingVertical: 14, paddingHorizontal: 20,
    borderRadius: 12, borderWidth: 1.5, borderColor: '#ddd',
  },
  clearBtnText: { color: '#666', fontWeight: '600', fontSize: 15 },
  checkoutBtn: {
    flex: 1, backgroundColor: '#f4a261',
    borderRadius: 12, paddingVertical: 14, alignItems: 'center',
  },
  checkoutBtnText: { color: 'white', fontWeight: '700', fontSize: 16 },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32 },
  emptyIcon: { fontSize: 64, marginBottom: 16 },
  emptyTitle: { fontSize: 20, fontWeight: '700', color: '#1a1a2e', marginBottom: 8 },
  emptySub: { fontSize: 14, color: '#888', textAlign: 'center', marginBottom: 24 },
  browseBtn: {
    backgroundColor: '#f4a261', borderRadius: 12,
    paddingHorizontal: 24, paddingVertical: 14,
  },
  browseBtnText: { color: 'white', fontWeight: '700', fontSize: 15 },
});
