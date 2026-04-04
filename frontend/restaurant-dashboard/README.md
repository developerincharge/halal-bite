# RestaurantDashboard

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.2.3.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
# Halal Bite — Restaurant Dashboard

Angular 17 standalone components app for restaurant owners to manage their menus and orders.

## Prerequisites

- Node.js 18+
- Angular CLI: `npm install -g @angular/cli`
- Backend services running (ports 8080–8087)

## Setup

```bash
cd frontend/restaurant-dashboard
npm install
npm start
```

App runs at **http://localhost:4200**

## Pages

| Route | Description |
|---|---|
| `/login` | Sign in as restaurant owner |
| `/register` | Create a new restaurant owner account |
| `/dashboard` | Overview — active orders + restaurant stats |
| `/menu` | Manage categories and menu items |
| `/orders` | Active orders and order history |
| `/orders/:id` | Order detail — update status |

## Test Flow

1. Register a new account at `/register` (automatically RESTAURANT_OWNER role)
2. Dashboard will prompt you to set up your restaurant in the Menu section
3. Create menu categories (Burgers, Drinks, etc.)
4. Add menu items under each category
5. Open Postman and place a customer order using the API
6. Watch the order appear live on `/orders`
7. Click the order → advance status → PREPARING → READY → DELIVERED

## API Configuration

Edit `src/environments/environment.ts` to change the backend URL:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

## Architecture

- **Standalone components** — Angular 17 style, no NgModules
- **Signals** — reactive state without RxJS complexity where possible
- **Lazy loading** — every route loads its component on demand
- **JWT interceptor** — auto-attaches Bearer token to every API call
- **Auth guard** — redirects unauthenticated users to login
