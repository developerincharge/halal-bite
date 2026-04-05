import React, { useEffect, useState } from 'react';
import {
  View, Text, SectionList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert,
} from 'react-native';
import { menuApi, MenuCategory, MenuItem } from '../../services/api';
import { useCart } from '../../context/CartContext';

export default function MenuScreen({ route, navigation }: any) {
  const { restaurantId, restaurantName } = route.params;
  const { addItem, totalItems, totalAmount, restaurantId: cartRestaurantId } = useCart();
  const [categories, setCategories] = useState<MenuCategory[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    navigation.setOptions({ title: restaurantName });
    loadMenu();
  }, []);

  const loadMenu = async () => {
    try {
      const { data } = await menuApi.getCategories(restaurantId);
      setCategories(data.filter(c => c.isActive && c.items.length > 0));
    } catch (e) {
      Alert.alert('Error', 'Failed to load menu');
    } finally {
      setLoading(false);
    }
  };

  const handleAddItem = (item: MenuItem) => {
    if (!item.isAvailable) return;

    // Warn if adding from a different restaurant
    if (cartRestaurantId && cartRestaurantId !== restaurantId) {
      Alert.alert(
        'Start New Order?',
        'Your cart has items from another restaurant. Adding this item will clear your cart.',
        [
          { text: 'Cancel', style: 'cancel' },
          { text: 'Clear & Add', onPress: () => addItem(item, restaurantId, restaurantName) },
        ]
      );
      return;
    }

    addItem(item, restaurantId, restaurantName);
  };

  const sections = categories.map(cat => ({
    title: cat.name,
    data: cat.items.filter(i => i.isAvailable),
  }));

  const renderItem = ({ item }: { item: MenuItem }) => (
    <View style={styles.itemCard}>
      <View style={styles.itemInfo}>
        <Text style={styles.itemName}>{item.name}</Text>
        {item.description ? (
          <Text style={styles.itemDesc} numberOfLines={2}>{item.description}</Text>
        ) : null}
        <View style={styles.itemBadges}>
          {item.isSpicy && <Text style={styles.badge}>🌶️ Spicy</Text>}
          {item.isVegan && <Text style={styles.badge}>🌿 Vegan</Text>}
          {item.isGlutenFree && <Text style={styles.badge}>GF</Text>}
          {item.calories ? (
            <Text style={styles.badge}>{item.calories} cal</Text>
          ) : null}
        </View>
        <View style={styles.itemBottom}>
          {item.discountedPrice ? (
            <>
              <Text style={styles.priceOriginal}>${item.price.toFixed(2)}</Text>
              <Text style={styles.priceDiscounted}>${item.discountedPrice.toFixed(2)}</Text>
            </>
          ) : (
            <Text style={styles.price}>${item.price.toFixed(2)}</Text>
          )}
        </View>
      </View>
      <TouchableOpacity style={styles.addBtn} onPress={() => handleAddItem(item)}>
        <Text style={styles.addBtnText}>+</Text>
      </TouchableOpacity>
    </View>
  );

  const renderSectionHeader = ({ section }: any) => (
    <View style={styles.sectionHeader}>
      <Text style={styles.sectionTitle}>{section.title}</Text>
    </View>
  );

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#f4a261" />
      </View>
    );
  }

return (
  <View style={styles.container}>
    <SectionList
      sections={sections}
      keyExtractor={(item) => item.id}
      renderItem={renderItem}
      renderSectionHeader={renderSectionHeader}
      contentContainerStyle={styles.list}
      stickySectionHeadersEnabled
      ListEmptyComponent={
        <View style={styles.center}>
          <Text style={styles.emptyText}>No items available</Text>
        </View>
      }
    />

    {/* Cart FAB — fixed at bottom */}
    {totalItems > 0 && (
      <View style={styles.cartFabContainer}>
        <TouchableOpacity
          style={styles.cartFab}
          onPress={() => navigation.navigate('Cart')}
        >
          <View style={styles.cartBadge}>
            <Text style={styles.cartBadgeText}>{totalItems}</Text>
          </View>
          <Text style={styles.cartFabText}>View Cart</Text>
          <Text style={styles.cartFabAmount}>${Number(totalAmount).toFixed(2)}</Text>
        </TouchableOpacity>
      </View>
    )}
  </View>
);
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f8f9fa' },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  list: { paddingBottom: 100 },
  sectionHeader: {
    backgroundColor: '#f8f9fa', paddingHorizontal: 16,
    paddingTop: 20, paddingBottom: 8,
  },
  sectionTitle: { fontSize: 18, fontWeight: '800', color: '#1a1a2e' },
  itemCard: {
    flexDirection: 'row', backgroundColor: 'white',
    marginHorizontal: 16, marginBottom: 10, borderRadius: 12,
    padding: 14, shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05,
    shadowRadius: 4, elevation: 2,
  },
  itemInfo: { flex: 1, marginRight: 12 },
  itemName: { fontSize: 15, fontWeight: '700', color: '#1a1a2e' },
  itemDesc: { fontSize: 12, color: '#888', marginTop: 3, lineHeight: 17 },
  itemBadges: { flexDirection: 'row', flexWrap: 'wrap', gap: 4, marginTop: 6 },
  badge: {
    fontSize: 10, color: '#666', backgroundColor: '#f0f0f0',
    paddingHorizontal: 6, paddingVertical: 2, borderRadius: 6,
  },
  itemBottom: { flexDirection: 'row', alignItems: 'center', marginTop: 8, gap: 6 },
  price: { fontSize: 15, fontWeight: '700', color: '#1a1a2e' },
  priceOriginal: {
    fontSize: 12, color: '#aaa',
    textDecorationLine: 'line-through',
  },
  priceDiscounted: { fontSize: 15, fontWeight: '700', color: '#e63946' },
  addBtn: {
    width: 36, height: 36, borderRadius: 18,
    backgroundColor: '#f4a261', alignItems: 'center', justifyContent: 'center',
    alignSelf: 'center',
  },
  addBtnText: { color: 'white', fontSize: 22, fontWeight: '700', lineHeight: 26 },
  emptyText: { color: '#888', fontSize: 15 },
cartFabContainer: {
  backgroundColor: 'transparent',
  paddingHorizontal: 16,
  paddingBottom: 16,
  paddingTop: 8,
},
cartFab: {
  backgroundColor: '#1a1a2e',
  borderRadius: 16,
  flexDirection: 'row',
  alignItems: 'center',
  paddingHorizontal: 20,
  paddingVertical: 14,
  shadowColor: '#000',
  shadowOffset: { width: 0, height: 4 },
  shadowOpacity: 0.3,
  shadowRadius: 12,
  elevation: 8,
},
  cartBadge: {
    backgroundColor: '#f4a261', width: 24, height: 24, borderRadius: 12,
    alignItems: 'center', justifyContent: 'center', marginRight: 12,
  },
  cartBadgeText: { color: 'white', fontSize: 12, fontWeight: '700' },
  cartFabText: { flex: 1, color: 'white', fontSize: 15, fontWeight: '700' },
  cartFabAmount: { color: '#f4a261', fontSize: 15, fontWeight: '700' },
});
