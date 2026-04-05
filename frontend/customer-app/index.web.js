import { registerRootComponent } from 'expo';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import React from 'react';
import App from './App';

function Root() {
  return (
    <GestureHandlerRootView style={{ flex: 1, height: '100vh' }}>
      <App />
    </GestureHandlerRootView>
  );
}

registerRootComponent(Root);