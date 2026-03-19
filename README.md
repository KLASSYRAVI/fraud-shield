# Real-Time Fraud Detection & Risk Analytics Platform

- Built a real-time fraud detection platform using microservices architecture
- Integrated Python XGBoost ML model via REST API into Java Spring Boot backend
- Implemented JWT-based authentication and role-based access control
- Containerized all services using Docker Compose for one-command deployment
- Processed transactions with risk scoring latency under 200ms
- Designed PostgreSQL schema for financial transaction storage and analytics

## Major Upgrades Include
- Implemented WebSocket real-time transaction streaming
- Built rule-based compliance engine with ML integration
- Designed admin case management workflow with audit trails
- Documented REST APIs using OpenAPI/Swagger
- Implemented CSV reporting for regulatory compliance

## Setup Steps
1. Make sure you have Docker and Docker Compose installed.
2. Clone this repository and navigate to the root directory `fraud-detection-platform`.
3. Run the complete stack using Docker Compose:
   ```bash
   docker-compose up --build
   ```
4. Access the different services:
   - Frontend Dashboard: `http://localhost:3000`
   - Backend API: `http://localhost:8081`
   - Swagger / OpenAPI Docs: `http://localhost:8081/swagger-ui.html`
   - ML Service API Docs: `http://localhost:8000/docs`
   - Database: `localhost:5432`

## API Endpoints List

### Auth
- `POST /api/auth/login` - Login User (returns JWT)
- `POST /api/auth/register` - Register User

### Transactions
- `POST /api/transactions/simulate` - Generate a random transaction & get risk scored.
- `GET /api/transactions` - Paginated list of transactions
- `GET /api/transactions/flagged` - Flagged high-risk transactions
- `GET /api/transactions/pending-review` - Flagged cases waiting for admin action
- `PATCH /api/transactions/{id}/approve` - Approve a case 
- `PATCH /api/transactions/{id}/reject` - Reject a case

### Dashboard
- `GET /api/dashboard/stats` - Summary stats for dashboard
- `GET /api/dashboard/trends` - Hourly trends, devices, areas for analytics
- `GET /api/dashboard/export` - Export transactions as CSV 

### Admin
- `GET /api/admin/audit-logs` - Application audit history

### ML Service (internal)
- `POST /predict` - Return risk score and probability
