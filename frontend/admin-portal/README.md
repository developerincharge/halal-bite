# Halal Bite — Admin Portal

Next.js 14 admin portal for platform management.

## Prerequisites

- Node.js 18+
- All backend services running

## Setup

```bash
cd frontend/admin-portal
npm install
npm run dev
```

App runs at **http://localhost:4201**

## First Login

You need an ADMIN account. Create one via Postman:

```
POST http://localhost:8080/api/v1/auth/register
Body: {
  "email": "admin@halalbite.com",
  "password": "admin123456",
  "role": "ADMIN"
}
```

Then log in at http://localhost:4201 with those credentials.

## Pages

| Page | Description |
|---|---|
| `/` | Login — ADMIN role required |
| `/dashboard` | Stats overview + pending restaurants |
| `/restaurants` | All restaurants with status filter tabs |
| `/restaurants/{id}` | Restaurant detail — approve, suspend, reinstate |
| `/orders` | Orders by restaurant |

## Key Feature — Approve Restaurants

1. Restaurant owners register on the dashboard (port 4200)
2. Their restaurant starts as **PENDING**
3. Admin logs in here → Dashboard shows all pending restaurants
4. Click **Review →** → click **Approve** ✅
5. Restaurant is now **ACTIVE** and visible to customers in the app

## CORS

The gateway allows port 4201 already from the SecurityConfig we updated.
If you get CORS errors, make sure `http://localhost:4201` is in the
gateway's `corsConfigurationSource()` allowed origins list.
