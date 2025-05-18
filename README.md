# 🚲 Eiffel Bike Rental System

This project is a web-based application developed for Eiffel Bike Corp. It allows students and employees of Université Gustave Eiffel to rent or purchase bicycles through an intuitive online platform. The system includes rental management, purchase options, a waiting list, and integration with external services.

## ✅ Main Features

- **Bike Rental**: Users can rent available bikes for a defined period.
- **Bike Purchase**: Users can purchase bikes through the platform.
- **Waiting List System**: If no bikes are available, users can join a waiting list.
- **Currency Conversion**: Purchase prices can be viewed in multiple currencies.
- **Payment Handling**: Payment processing simulation for both rentals and purchases.
- **External Service Integration**: The app connects to an external web service to retrieve product information and prices.

## 🧰 Technologies Used

- **Java (JDK 11+)**
- **HTML/CSS**
- **RESTful APIs**
- **Apache Tomcat** (server)
- **Eclipse IDE** (Dynamic Web Project)
- **MySQL** (database)

## 🗄️ Database

The application uses **MySQL** to store and manage data such as:

- Users
- Bikes
- Rentals
- Purchases
- Waiting list entries

📂 Project Structure

EiffelBikeRental/
├── WebContent/

│   ├── index.html

│   ├── rent.html

│   ├── buy.html

│   └── assets/ (CSS, images, JS)

├── java/

│   ├── models/         # Domain classes (Bike, User, Order, etc.)

│   ├── services/       # Business logic

│   ├── controllers/    # REST API endpoints

│   └── utils/          # Helper classes (e.g., DB connection, currency conversion)

├── WEB-INF/

│   └── web.xml         # Web application deployment descriptor

└── README.md

🔌 Example API Endpoints
GET /api/bikes — Returns list of available bikes

POST /api/rent — Rent a bike

POST /api/buy — Buy a bike

POST /api/waitlist — Add to waiting list

GET /api/convert/EUR/USD — Get currency conversion rate

POST /api/payment — Simulate payment transaction
