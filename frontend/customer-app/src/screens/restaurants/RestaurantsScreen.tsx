import React, { useEffect, useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  TextInput, ActivityIndicator, RefreshControl,
} from 'react-native';
import { restaurantApi, Restaurant } from '../../services/api';
import { useAuth } from '../../context/AuthContext';

export default function RestaurantsScreen({ navigation }: any) {
  const { user, logout } = useAuth();
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    loadRestaurants();
  }, []);

const loadRestaurants = async () => {
  try {
    console.log('Fetching restaurants from API...');
    const { data } = await restaurantApi.getAll();
    console.log('API response:', JSON.stringify(data));
    console.log('Content array:', data?.content);
    
    const list = data?.content ?? [];
    console.log('Restaurant count:', list.length);
    setRestaurants(list);
  } catch (e: any) {
    console.error('Error loading restaurants:', e?.message);
    console.error('Error response:', e?.response?.data);
    console.error('Error status:', e?.response?.status);
  } finally {
    setLoading(false);
    setRefreshing(false);
  }
};

  const handleSearch = async (query: string) => {
    setSearch(query);
    if (query.length < 2) {
      if (query.length === 0) loadRestaurants();
      return;
    }
    setSearching(true);
    try {
      const { data } = await restaurantApi.search(query);
      setRestaurants(data.content);
    } catch (e) {
      console.error('Search failed', e);
    } finally {
      setSearching(false);
    }
  };

  const renderRestaurant = ({ item }: { item: Restaurant }) => (
    <TouchableOpacity
      style={styles.card}
      onPress={() => navigation.navigate('Menu', {
        restaurantId: item.id,
        restaurantName: item.name,
      })}
    >
      <View style={styles.cardEmoji}>
        <Text style={styles.emoji}>🥙</Text>
      </View>
      <View style={styles.cardInfo}>
        <Text style={styles.cardName}>{item.name}</Text>
        <Text style={styles.cardCuisine}>{item.cuisineType}</Text>
        <View style={styles.cardMeta}>
          <Text style={styles.metaText}>⭐ {item.averageRating?.toFixed(1) || 'New'}</Text>
          <Text style={styles.metaDot}>·</Text>
          <Text style={styles.metaText}>🕐 {item.estimatedDeliveryMinutes} min</Text>
          <Text style={styles.metaDot}>·</Text>
          <Text style={styles.metaText}>Min ${item.minimumOrderAmount}</Text>
        </View>
      </View>
      <Text style={styles.arrow}>›</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>Hello 👋</Text>
          <Text style={styles.subGreeting}>{user?.email}</Text>
        </View>
        <TouchableOpacity style={styles.logoutBtn} onPress={logout}>
          <Text style={styles.logoutText}>Sign Out</Text>
        </TouchableOpacity>
      </View>

      {/* Search bar */}
      <View style={styles.searchBar}>
        <Text style={styles.searchIcon}>🔍</Text>
        <TextInput
          style={styles.searchInput}
          value={search}
          onChangeText={handleSearch}
          placeholder="Search restaurants or cuisine..."
          placeholderTextColor="#aaa"
        />
        {searching && <ActivityIndicator size="small" color="#f4a261" />}
      </View>

      <Text style={styles.sectionTitle}>
        {search ? `Results for "${search}"` : 'Restaurants Near You'}
      </Text>

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color="#f4a261" />
        </View>
      ) : restaurants.length === 0 ? (
        <View style={styles.center}>
          <Text style={styles.emptyIcon}>🍽️</Text>
          <Text style={styles.emptyText}>No restaurants found</Text>
        </View>
      ) : (
        <FlatList
          data={restaurants}
          keyExtractor={(item) => item.id}
          renderItem={renderRestaurant}
          contentContainerStyle={styles.list}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={() => { setRefreshing(true); loadRestaurants(); }}
              tintColor="#f4a261"
            />
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f8f9fa' },
  header: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    backgroundColor: '#1a1a2e', paddingTop: 56, paddingBottom: 20, paddingHorizontal: 20,
  },
  greeting: { fontSize: 22, fontWeight: '700', color: 'white' },
  subGreeting: { fontSize: 12, color: 'rgba(255,255,255,0.5)', marginTop: 2 },
  logoutBtn: {
    paddingHorizontal: 12, paddingVertical: 6,
    backgroundColor: 'rgba(255,255,255,0.1)', borderRadius: 8,
  },
  logoutText: { color: 'rgba(255,255,255,0.7)', fontSize: 13 },
  searchBar: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: 'white', marginHorizontal: 16, marginTop: 16,
    borderRadius: 12, paddingHorizontal: 14, paddingVertical: 10,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06, shadowRadius: 8, elevation: 3,
  },
  searchIcon: { fontSize: 16, marginRight: 8 },
  searchInput: { flex: 1, fontSize: 15, color: '#333' },
  sectionTitle: {
    fontSize: 16, fontWeight: '700', color: '#1a1a2e',
    marginHorizontal: 20, marginTop: 20, marginBottom: 12,
  },
  list: { paddingHorizontal: 16, paddingBottom: 24 },
  card: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: 'white', borderRadius: 14, marginBottom: 12,
    padding: 14, shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.06,
    shadowRadius: 8, elevation: 3,
  },
  cardEmoji: {
    width: 56, height: 56, borderRadius: 12,
    backgroundColor: '#fff3e0', alignItems: 'center', justifyContent: 'center',
  },
  emoji: { fontSize: 28 },
  cardInfo: { flex: 1, marginLeft: 14 },
  cardName: { fontSize: 15, fontWeight: '700', color: '#1a1a2e' },
  cardCuisine: { fontSize: 12, color: '#f4a261', marginTop: 2, fontWeight: '600' },
  cardMeta: { flexDirection: 'row', alignItems: 'center', marginTop: 6 },
  metaText: { fontSize: 12, color: '#888' },
  metaDot: { color: '#ccc', marginHorizontal: 4 },
  arrow: { fontSize: 22, color: '#ccc', marginLeft: 8 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  emptyIcon: { fontSize: 48, marginBottom: 12 },
  emptyText: { fontSize: 15, color: '#888' },
});
