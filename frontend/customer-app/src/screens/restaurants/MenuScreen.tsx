
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
    console.log('Categories raw response:', JSON.stringify(data));
    console.log('Category count:', data?.length);
    data?.forEach((cat: any) => {
      console.log(`Category: ${cat.name}, isActive: ${cat.isActive}, items: ${cat.items?.length}`);
      cat.items?.forEach((item: any) => {
        console.log(`  Item: ${item.name}, isAvailable: ${item.isAvailable}`);
      });
    });
    setCategories(data ?? []);
  } catch (e) {
    console.error('Menu load error:', e);
    Alert.alert('Error', 'Failed to load menu');
  } finally {
    setLoading(false);
  }
};

  const handleAddItem = (item: MenuItem) => {
    if (!item.isAvailable) {
      Alert.alert('Unavailable', 'This item is currently unavailable');
      return;
    }

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

  // Show all items — both available and unavailable (greyed out)
    const sections = categories
        .map(cat => ({
       title: cat.name,
       data: cat.items ?? [],
       })).filter(s => s.data.length > 0);


  const renderItem = ({ item }: { item: MenuItem }) => (
    <View style={[styles.itemCard, !item.isAvailable && styles.itemUnavailable]}>
      <View style={styles.itemInfo}>
        <Text style={styles.itemName}>{item.name}</Text>
        {item.description ? (
          <Text style={styles.itemDesc} numberOfLines={2}>{item.description}</Text>
        ) : null}
        <View style={styles.itemBadges}>
          {item.isSpicy && <Text style={styles.badge}>🌶️ Spicy</Text>}
          {item.isVegan && <Text style={styles.badge}>🌿 Vegan</Text>}
          {item.isGlutenFree && <Text style={styles.badge}>GF</Text>}
          {item.calories ? <Text style={styles.badge}>{item.calories} cal</Text> : null}
          {!item.isAvailable && <Text style={[styles.badge, styles.badgeUnavailable]}>Unavailable</Text>}
        </View>
        <View style={styles.itemBottom}>
          {item.discountedPrice ? (
            <>
              <Text style={styles.priceOriginal}>
                ${parseFloat(String(item.price)).toFixed(2)}
              </Text>
              <Text style={styles.priceDiscounted}>
                ${parseFloat(String(item.discountedPrice)).toFixed(2)}
              </Text>
            </>
          ) : (
            <Text style={styles.price}>
              ${parseFloat(String(item.price)).toFixed(2)}
            </Text>
          )}
        </View>
      </View>
      <TouchableOpacity
        style={[styles.addBtn, !item.isAvailable && styles.addBtnDisabled]}
        onPress={() => handleAddItem(item)}
        disabled={!item.isAvailable}
      >
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

if (categories.length === 0) {
  return (
    <View style={styles.center}>
      <Text style={{ fontSize: 48, marginBottom: 12 }}>🍽️</Text>
      <Text style={{ fontSize: 16, fontWeight: '700', color: '#1a1a2e' }}>
        No categories yet
      </Text>
      <Text style={{ fontSize: 13, color: '#888', marginTop: 6, textAlign: 'center' }}>
        The restaurant hasn't added any menu items yet
      </Text>
    </View>
  );
}

if (sections.length === 0) {
  return (
    <View style={styles.center}>
      <Text style={{ fontSize: 48, marginBottom: 12 }}>🍽️</Text>
      <Text style={{ fontSize: 16, fontWeight: '700', color: '#1a1a2e' }}>
        No items available
      </Text>
      <Text style={{ fontSize: 13, color: '#888', marginTop: 6, textAlign: 'center' }}>
        All items are currently unavailable
      </Text>
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
    />
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
          <Text style={styles.cartFabAmount}>
            ${Number(totalAmount).toFixed(2)}
          </Text>
        </TouchableOpacity>
      </View>
    )}
  </View>
);
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f8f9fa' },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32 },
  list: { paddingBottom: 16 },

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
  itemUnavailable: { opacity: 0.5 },
  itemInfo: { flex: 1, marginRight: 12 },
  itemName: { fontSize: 15, fontWeight: '700', color: '#1a1a2e' },
  itemDesc: { fontSize: 12, color: '#888', marginTop: 3, lineHeight: 17 },
  itemBadges: { flexDirection: 'row', flexWrap: 'wrap', gap: 4, marginTop: 6 },
  badge: {
    fontSize: 10, color: '#666', backgroundColor: '#f0f0f0',
    paddingHorizontal: 6, paddingVertical: 2, borderRadius: 6,
  },
  badgeUnavailable: {
    backgroundColor: '#f8d7da', color: '#721c24',
  },
  itemBottom: { flexDirection: 'row', alignItems: 'center', marginTop: 8, gap: 6 },
  price: { fontSize: 15, fontWeight: '700', color: '#1a1a2e' },
  priceOriginal: {
    fontSize: 12, color: '#aaa', textDecorationLine: 'line-through',
  },
  priceDiscounted: { fontSize: 15, fontWeight: '700', color: '#e63946' },

  addBtn: {
    width: 36, height: 36, borderRadius: 18,
    backgroundColor: '#f4a261', alignItems: 'center', justifyContent: 'center',
    alignSelf: 'center',
  },
  addBtnDisabled: { backgroundColor: '#ccc' },
  addBtnText: { color: 'white', fontSize: 22, fontWeight: '700', lineHeight: 26 },

  emptyText: { fontSize: 16, fontWeight: '700', color: '#1a1a2e', marginTop: 12 },
  emptySubText: { fontSize: 13, color: '#888', marginTop: 6, textAlign: 'center' },
  emptyIcon: { fontSize: 48 },

  cartFabContainer: {
    paddingHorizontal: 16, paddingBottom: 16, paddingTop: 8,
    backgroundColor: 'transparent',
  },
  cartFab: {
    backgroundColor: '#1a1a2e', borderRadius: 16,
    flexDirection: 'row', alignItems: 'center',
    paddingHorizontal: 20, paddingVertical: 14,
    shadowColor: '#000', shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3, shadowRadius: 12, elevation: 8,
  },
  cartBadge: {
    backgroundColor: '#f4a261', width: 24, height: 24, borderRadius: 12,
    alignItems: 'center', justifyContent: 'center', marginRight: 12,
  },
  cartBadgeText: { color: 'white', fontSize: 12, fontWeight: '700' },
  cartFabText: { flex: 1, color: 'white', fontSize: 15, fontWeight: '700' },
  cartFabAmount: { color: '#f4a261', fontSize: 15, fontWeight: '700' },
});