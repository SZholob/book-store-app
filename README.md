# Book Store App

An educational project: a web-based book store application developed using the Spring Framework and the MVC architectural pattern.

## Tech Stack
* **Programming Language:** Java
* **Framework:** Spring Boot (Spring MVC, Spring Data JPA, Spring Security)
* **View:** Thymeleaf, HTML
* **Build Tool:** Maven
* **Utilities & Libraries:** Lombok, ModelMapper, Validation API
* **Database:** Relational DB (SQL)

## Features and Roles

The system supports role-based access control with three main levels of interaction:

**1. Any Registered User:**
* Browse the catalog of available books.
* View detailed information about a selected book.
* Edit personal information and manage the user profile.

**2. Customers:**
* Add books to the shopping cart.
* Place purchase orders.
* Delete their own account.

**3. Employees:**
* Perform CRUD operations on books (add, edit, delete).
* Manage orders (confirm orders placed by customers).
* Administer users (block/unblock customer accounts, view the list of registered customers).

## Implementation Details
* **Architecture:** Multi-layered architecture (Controllers, Services, Repositories).
* **Data Transfer Objects (DTO):** Used for secure data transfer between layers (mapping implemented via ModelMapper).
* **Security:** Authentication and authorization configured using Spring Security.
* **Localization (i18n):** Support for internationalization (English and other languages).
* **Exception Handling & Validation:** Implemented data integrity checks and custom exception classes for graceful error handling.

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/SZholob/book-store-app.git
cd book-store-app
```

### 2. Run the application

**Option A: Using an IDE (IntelliJ IDEA, Eclipse, etc.)**
1. Open the project in your IDE.
2. Wait for Maven to resolve and download dependencies.
3. Find and open the `BookStoreServiceSolutionApplication` class.
4. Run the `main()` method.

**Option B: Using Maven (Command Line)**
1. Build the project:
   ```bash
   mvn clean install
   ```
2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

*(SQL scripts located in the `src/main/resources/sql` directory are used for database initialization)*