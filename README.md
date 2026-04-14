# halal-bite
Food-delivery-app a project for Restaurant Food Ordering directly from the web or mobile


## What's in this folder

```
halal-bite/
├── docker-compose.yml          ← starts everything
├── Dockerfile                  ← template for all services
├── application-docker.yml      ← Spring docker profile
├── .env.example                ← environment variable template
└── .github/
    └── workflows/
        └── ci-cd.yml           ← GitHub Actions pipeline
```

---

## Step 1 — Add Dockerfile to every service

Copy the `Dockerfile` into each backend service directory:

```bash
# Run from the halal-bite root directory
for service in auth-service user-service restaurant-service menu-service order-service payment-service notification-service api-gateway service-registry; do
  cp Dockerfile backend/$service/Dockerfile
done
```

---

## Step 2 — Add docker profile to every service

Copy `application-docker.yml` into each service's resources folder:

```bash
for service in auth-service user-service restaurant-service menu-service order-service payment-service notification-service api-gateway service-registry; do
  cp application-docker.yml backend/$service/src/main/resources/application-docker.yml
done
```

---

## Step 3 — Create your .env file

```bash
cp .env.example .env
```

Open `.env` and fill in your PayPal sandbox credentials and mail credentials.

---

## Step 4 — Add .env to .gitignore

Open `.gitignore` at the project root and add:
```
.env
```

---

## Step 5 — Start everything

```bash
# Start infrastructure only (postgres, kafka, redis)
docker-compose up -d postgres redis kafka kafka-ui

# Wait ~30 seconds then start all services
docker-compose up -d

# Check everything is running
docker-compose ps

# Watch logs from all services
docker-compose logs -f

# Watch logs from one service
docker-compose logs -f order-service
```

---

## Useful commands

```bash
# Stop everything
docker-compose down

# Stop and remove all data (fresh start)
docker-compose down -v

# Rebuild one service after code change
docker-compose build order-service
docker-compose up -d order-service

# Rebuild all services
docker-compose build
docker-compose up -d

# Check health of all containers
docker-compose ps

# Shell into a service
docker exec -it halal-bite-order-service sh

# Check database
docker exec -it halal-bite-postgres psql -U halalbiteuser -d order_service_db
```

---

## Service URLs (same as before)

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Auth Service | http://localhost:8081 |
| User Service | http://localhost:8082 |
| Restaurant Service | http://localhost:8083 |
| Menu Service | http://localhost:8084 |
| Order Service | http://localhost:8085 |
| Payment Service | http://localhost:8086 |
| Notification Service | http://localhost:8087 |
| Eureka Dashboard | http://localhost:8761 |
| Kafka UI | http://localhost:8090 |

---

## GitHub Actions CI/CD

### Setup

1. Push your code to GitHub
2. Go to your repo → Settings → Secrets → Actions
3. Add these secrets:
   ```
   PAYPAL_CLIENT_ID      → your sandbox client ID
   PAYPAL_CLIENT_SECRET  → your sandbox secret
   MAIL_USERNAME         → mailtrap username
   MAIL_PASSWORD         → mailtrap password
   ```

### What the pipeline does

On every push to `main`:
1. Runs all unit tests for all 8 services in parallel
2. If tests pass, builds Docker images for all services
3. Pushes images to GitHub Container Registry (ghcr.io)
4. Builds all 3 frontend apps

On `develop` branch or pull requests:
1. Only runs tests — no Docker build

### View pipeline runs

Go to your GitHub repo → Actions tab

---

## Troubleshooting

**Service fails to start:**
```bash
docker-compose logs auth-service
```

**Port already in use:**
```bash
# Find what's using port 8081
netstat -ano | findstr :8081
# Kill the process
taskkill /PID <pid> /F
```

**Postgres connection refused:**
```bash
# Check postgres is healthy
docker inspect halal-bite-postgres | grep Health -A 5
```

**Kafka not ready:**
```bash
# Wait longer — Kafka takes ~30s to start
docker-compose logs kafka
```

