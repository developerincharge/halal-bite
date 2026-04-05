import React, { createContext, useContext, useEffect, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { authApi, AuthResponse } from '../services/api';

interface AuthContextType {
  user: AuthResponse | null;
  isLoading: boolean;
  isLoggedIn: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Load stored session on app start
  useEffect(() => {
    loadStoredUser();
  }, []);

  const loadStoredUser = async () => {
    try {
      const stored = await AsyncStorage.getItem('halalbite_user');
      if (stored) setUser(JSON.parse(stored));
    } catch (e) {
      console.error('Failed to load stored user', e);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (email: string, password: string) => {
    const { data } = await authApi.login(email, password);
    await AsyncStorage.setItem('halalbite_token', data.accessToken);
    await AsyncStorage.setItem('halalbite_user', JSON.stringify(data));
    setUser(data);
  };

  const register = async (email: string, password: string) => {
    const { data } = await authApi.register(email, password);
    await AsyncStorage.setItem('halalbite_token', data.accessToken);
    await AsyncStorage.setItem('halalbite_user', JSON.stringify(data));
    setUser(data);
  };

  const logout = async () => {
    await AsyncStorage.removeItem('halalbite_token');
    await AsyncStorage.removeItem('halalbite_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{
      user,
      isLoading,
      isLoggedIn: !!user,
      login,
      register,
      logout,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
