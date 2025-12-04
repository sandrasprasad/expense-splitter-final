# Expense Splitter

A full-stack expense sharing application similar to Splitwise, built with **Spring Boot**, **Oracle Database**, and **React**.

## 🚀 Features
- **User Management:** JWT Authentication, Login/Signup.
- **Group Management:** Create groups, add/remove members using Set logic.
- **Expense Tracking:**
  - Support for **EQUAL** and **EXACT** splits.
  - **BigDecimal** precision for financial accuracy.
- **Settlements:**
  - **Greedy Algorithm** to simplify debts (Who owes Whom).
  - Partial and Full settlement support.
- **Audit Trail:** Complete history of all transactions.
- **Dark Theme UI:** Modern, responsive interface.

## 🛠 Tech Stack
- **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), JPA/Hibernate.
- **Database:** Oracle Database.
- **Frontend:** React.js, Lucide Icons, Axios.

## ⚙️ Setup Instructions

### Backend
1. Navigate to `backend/`.
2. Update `application.properties` with your Oracle DB credentials.
3. Run `mvn spring-boot:run`.
4. Server starts on port `8082`.

### Frontend
1. Navigate to `frontend/`.
2. Run `npm install`.
3. Run `npm start`.
4. App opens at `http://localhost:3000`.

## 🧪 Testing
- **Backend:** Run `mvn test` to execute JUnit test cases for Services and Logic.
- **Frontend:** Run `npm test` to verify UI rendering.
