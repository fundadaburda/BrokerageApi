# Brokerage Firm API

A Spring Boot backend API for a brokerage firm that allows employees to manage stock orders for customers.

## Features

- **Order Management**: Create, list, and cancel stock orders
- **Asset Management**: Track customer assets and balances
- **Authentication & Authorization**: JWT-based authentication with role-based access control
- **Order Matching**: Admin functionality to match pending orders
- **H2 Database**: In-memory database for easy development and testing

## Requirements

- Java 21 or higher
- Gradle 8.5 or higher (included via Gradle Wrapper)

## Build and Run

### Build the project
```bash
cd brokerage-api
./gradlew build
```

### Run the application
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Run tests
```bash
./gradlew test
```

### Clean and build
```bash
./gradlew clean build
```

## API Documentation (Swagger)

The API documentation is automatically generated and available through Swagger UI:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Using Swagger UI

1. Navigate to `http://localhost:8080/swagger-ui.html`
2. To test authenticated endpoints:
   - First, use the `/api/auth/login` endpoint to get a JWT token
   - Click the "Authorize" button at the top of the page
   - Enter the token in the format: `Bearer <your-token>`
   - Click "Authorize" to apply the token to all requests
3. You can now test all endpoints directly from the Swagger UI

## H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:brokeragedb`
- Username: `sa`
- Password: (empty)

## Default Users

The application initializes with the following users:

| Username  | Password     | Role     | Initial Balance |
|-----------|--------------|----------|-----------------|
| admin     | admin123     | ADMIN    | -               |
| customer1 | password123  | CUSTOMER | 100,000 TRY     |
| customer2 | password123  | CUSTOMER | 50,000 TRY + 100 AAPL |

## API Endpoints

### Authentication

#### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "customer1",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "customer1",
  "role": "CUSTOMER",
  "customerId": 2
}
```

### Orders

All order endpoints require authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

#### Create Order
```bash
POST /api/orders
Content-Type: application/json
Authorization: Bearer <token>

{
  "customerId": 2,
  "assetName": "AAPL",
  "side": "BUY",
  "size": 10,
  "price": 150.50
}
```

#### List Orders
```bash
GET /api/orders?customerId=2&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59&status=PENDING
Authorization: Bearer <token>
```

Parameters:
- `customerId` (required): Customer ID
- `startDate` (required): Start date in ISO format
- `endDate` (required): End date in ISO format
- `status` (optional): Filter by status (PENDING, MATCHED, CANCELED)

#### Cancel Order
```bash
DELETE /api/orders/{orderId}?customerId=2
Authorization: Bearer <token>
```

### Assets

#### List Assets
```bash
GET /api/assets?customerId=2&assetName=AAPL
Authorization: Bearer <token>
```

Parameters:
- `customerId` (required): Customer ID
- `assetName` (optional): Filter by asset name

### Admin Operations

#### Match Orders (Admin only)
```bash
POST /api/admin/match-orders
Content-Type: application/json
Authorization: Bearer <admin-token>

{
  "orderIds": [1, 2, 3]
}
```

## Business Rules

1. **Order Creation**:
   - BUY orders: Checks and reserves TRY balance (size × price)
   - SELL orders: Checks and reserves asset balance
   - Orders are created with PENDING status

2. **Order Cancellation**:
   - Only PENDING orders can be canceled
   - Reserved amounts are returned to usable balance

3. **Order Matching**:
   - Only ADMIN users can match orders
   - BUY orders: Increases asset balance
   - SELL orders: Increases TRY balance
   - Order status changes to MATCHED

4. **Asset Management**:
   - Each customer has assets with `size` (total) and `usableSize` (available)
   - TRY is treated as an asset
   - All trades are against TRY

## Testing Examples

### Example 1: Buy Order
```bash
# Login as customer1
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer1","password":"password123"}'

# Create buy order for 10 AAPL shares at 150 TRY each
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"customerId":2,"assetName":"AAPL","side":"BUY","size":10,"price":150}'
```

### Example 2: Sell Order
```bash
# Login as customer2 (has AAPL shares)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer2","password":"password123"}'

# Create sell order for 5 AAPL shares at 160 TRY each
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"customerId":3,"assetName":"AAPL","side":"SELL","size":5,"price":160}'
```

### Example 3: Admin Match Orders
```bash
# Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Match pending orders
curl -X POST http://localhost:8080/api/admin/match-orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{"orderIds":[1,2]}'
```

## Project Structure

```
brokerage-api/
├── src/
│   ├── main/
│   │   ├── java/com/brokerage/
│   │   │   ├── config/         # Configuration classes
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── dto/           # Data transfer objects
│   │   │   ├── entity/        # JPA entities
│   │   │   ├── exception/     # Custom exceptions
│   │   │   ├── repository/    # Data repositories
│   │   │   ├── security/      # Security configuration
│   │   │   └── service/       # Business logic
│   │   └── resources/
│   │       └── application.properties
│   └── test/                  # Unit tests
├── pom.xml
└── README.md
```

## Bonus Features Implemented

**Bonus 1**: Customer authentication with login endpoint - each customer can only access their own data, while admin can access all data.

**Bonus 2**: Admin endpoint to match pending orders with automatic balance updates.

## Technologies Used

- Spring Boot 3.2.0
- Spring Security with JWT
- Spring Data JPA
- H2 Database
- Lombok
- JUnit 5 & Mockito for testing
- Maven for dependency management# BrokerageApi
A Spring Boot backend API for a brokerage firm that allows employees to manage stock orders for customers.
