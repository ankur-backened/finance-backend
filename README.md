# Finance Data Processing and Access Control Backend

A backend system built for managing financial records, users, and role-based access control. This project was built as part of a backend assessment for Zorvyn FinTech.

---

## Tech Stack

- **Language:** Java 17+
- **Framework:** Spring Boot 3.5.13
- **Database:** MySQL
- **ORM:** Spring Data JPA with Hibernate
- **Build Tool:** Maven
- **Utilities:** Lombok, Spring Validation

---

## Project Structure


src/main/java/com/zorvyn/finance_backend/
├── config/          → Web configuration and interceptor registration
├── controller/      → REST API endpoints
├── dto/             → Request and response data transfer objects
├── entity/          → Database table mappings
├── enums/           → Role, UserStatus, TransactionType
├── exception/       → Custom exceptions and global error handler
├── interceptor/     → Role based access control via X-User-Role header
├── repository/      → Database queries
└── service/         → Business logic



---

## How to Run Locally

### Prerequisites
- Java 17 or above
- MySQL running locally
- Maven installed

### Steps

1. Clone the repository
```bash
git clone https://github.com/ankur-backened/finance-backend.git
cd finance-backend
```

2. Create the database

No manual setup needed. The database `finance_db` is created automatically when the app starts thanks to `createDatabaseIfNotExist=true` in the config.

3. Update database credentials in `src/main/resources/application.properties`
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

4. Run the application
```bash
./mvnw spring-boot:run
```

Or simply run `FinanceBackendApplication.java` from your IDE.

5. Server starts at `http://localhost:8080`

---

## Access Control

Every API request must include the `X-User-Role` header.

| Role | GET | POST /api/records | POST /api/users | PUT | DELETE |
|------|-----|-------------------|-----------------|-----|--------|
| ADMIN | ✅ | ✅ | ✅ | ✅ | ✅ |
| ANALYST | ✅ | ✅ | ❌ | ❌ | ❌ |
| VIEWER | ✅ | ❌ | ❌ | ❌ | ❌ |

If the header is missing or contains an invalid role, the request is rejected with a `403 Forbidden` response.

---

## API Endpoints

### User Management

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | /api/users | Create a new user | ADMIN |
| GET | /api/users | Get all users | ANY |
| GET | /api/users/{id} | Get user by ID | ANY |
| PUT | /api/users/{id} | Update user | ADMIN |
| DELETE | /api/users/{id} | Soft delete user (sets status to INACTIVE) | ADMIN |

#### Create User — Request Body
```json
{
  "name": "Ankur Singh",
  "email": "ankur@zorvyn.com",
  "password": "pass123",
  "role": "ADMIN",
  "status": "ACTIVE"
}
```

#### Create User — Response
```json
{
  "id": 1,
  "name": "Ankur Singh",
  "email": "ankur@zorvyn.com",
  "role": "ADMIN",
  "status": "ACTIVE"
}
```

> Note: Password is never returned in any response.

---

### Financial Records Management

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | /api/records | Create a new record | ADMIN, ANALYST |
| GET | /api/records | Get all records (paginated) | ANY |
| GET | /api/records/{id} | Get record by ID | ANY |
| PUT | /api/records/{id} | Update a record | ADMIN |
| DELETE | /api/records/{id} | Delete a record | ADMIN |
| GET | /api/records/filter | Filter records | ANY |
| GET | /api/records/search | Search records by keyword | ANY |

#### Create Record — Request Body
```json
{
  "amount": 50000,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-01",
  "notes": "Monthly salary",
  "userId": 1
}
```

#### Pagination


GET /api/records?page=0&size=10

#### Filter Examples
GET /api/records/filter?type=INCOME
GET /api/records/filter?type=EXPENSE
GET /api/records/filter?category=Salary
GET /api/records/filter?userId=1
GET /api/records/filter?startDate=2026-01-01&endDate=2026-04-06

#### Search Example

GET /api/records/search?keyword=salary
Searches across both `category` and `notes` fields.

---

### Dashboard APIs

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | /api/dashboard/summary | Overall financial summary | ANY |
| GET | /api/dashboard/summary?userId=1 | Summary for a specific user | ANY |
| GET | /api/dashboard/monthly-trends | Month wise income and expense | ANY |
| GET | /api/dashboard/weekly-trends | Current week income and expense | ANY |

#### Dashboard Summary Response
```json
{
  "totalIncome": 75000.0,
  "totalExpense": 28000.0,
  "netBalance": 47000.0,
  "categoryWiseTotals": {
    "Salary": 50000.0,
    "Freelance": 25000.0,
    "Rent": 12000.0,
    "Food": 5000.0,
    "Transport": 3000.0
  },
  "recentTransactions": [...],
  "userWiseIncome": {
    "Ankur Singh": 75000.0
  },
  "userWiseExpense": {
    "Ankur Singh": 28000.0
  }
}
```

#### Monthly Trends Response
```json
[
  { "month": 4, "year": 2026, "type": "INCOME", "total": 75000.0 },
  { "month": 4, "year": 2026, "type": "EXPENSE", "total": 28000.0 }
]
```

#### Weekly Trends Response
```json
{
  "weekStart": "2026-04-06",
  "weekEnd": "2026-04-12",
  "weeklyIncome": 0.0,
  "weeklyExpense": 8000.0,
  "weeklyNetBalance": -8000.0
}
```

---

## Error Handling

All errors return a consistent JSON structure.
```json
{
  "timestamp": "2026-04-06T10:30:00",
  "status": 404,
  "message": "User not found with id: 99"
}
```

| Scenario | Status Code |
|----------|-------------|
| Resource not found | 404 |
| Access denied | 403 |
| Validation failed | 400 |
| Invalid role header | 403 |
| Missing role header | 403 |
| Server error | 500 |

#### Validation Error Response
```json
{
  "timestamp": "2026-04-06T10:30:00",
  "status": 400,
  "errors": {
    "name": "Name is required",
    "email": "Please provide a valid email",
    "password": "Password is required"
  }
}
```

---

## Database Schema

### users
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| name | VARCHAR | NOT NULL |
| email | VARCHAR | NOT NULL, UNIQUE |
| password | VARCHAR | NOT NULL |
| role | ENUM | ADMIN, ANALYST, VIEWER |
| status | ENUM | ACTIVE, INACTIVE |

### financial_records
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | Primary Key, Auto Increment |
| amount | DOUBLE | NOT NULL |
| type | ENUM | INCOME, EXPENSE |
| category | VARCHAR | NOT NULL |
| date | DATE | NOT NULL |
| notes | VARCHAR | Nullable |
| user_id | BIGINT | Foreign Key → users(id) |

---

## Assumptions and Design Decisions

**1. X-User-Role header instead of JWT**
Authentication via JWT was considered but intentionally skipped to keep the implementation clean and focused on backend logic and access control. The `X-User-Role` header clearly demonstrates role based behavior without introducing unnecessary complexity.

**2. Soft Delete for Users**
Instead of permanently deleting users from the database, the delete endpoint sets the user status to `INACTIVE`. This is a common real world pattern — data is preserved for audit and reporting purposes.

**3. Password not returned in responses**
Even though passwords are stored as plain text in this implementation, they are never included in any API response. In a production system, passwords would be BCrypt hashed.

**4. Flexible dashboard**
The dashboard summary accepts an optional `userId` parameter. Without it, it returns overall totals across all users. With it, it returns totals specific to that user. This makes the dashboard useful for both admin level and user level views.

**5. Paginated records**
The GET /api/records endpoint supports pagination with configurable page size. Default is page 0 with 10 records per page.

**6. Database auto creation**
The MySQL database `finance_db` is created automatically on first run using `createDatabaseIfNotExist=true`. Tables are also created and updated automatically by Hibernate.

---

## Enhancements Beyond Core Requirements

| Enhancement | Description |
|-------------|-------------|
| Soft Delete | Users are deactivated instead of deleted |
| Pagination | Records endpoint supports page and size parameters |
| Keyword Search | Search records by keyword across category and notes |
| User wise summary | Dashboard shows per user income and expense breakdown |
| Weekly trends | Current week income and expense summary |
| Monthly trends | Month wise income and expense breakdown |
| userId filter on dashboard | Get summary for a specific user |

---

## Author

**Ankur Kumar Singh**
- GitHub: [github.com/ankur-backened](https://github.com/ankur-backened)
- Email: ankursinghhhrajput4@gmail.com
