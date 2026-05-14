#!/bin/sh

# Replace placeholder with actual Railway environment variable
find /app/dist -name "*.js" -exec sed -i "s|RAILWAY_API_URL_PLACEHOLDER|$API_URL|g" {} \;

# Start the server
npx serve -s dist/restaurant-dashboard/browser -l $PORT