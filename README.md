# Price Comparator Backend


## Overview
This project is a backend application for comparing product prices, optimizing shopping baskets, and analyzing discounts across multiple stores. It is written in Java and uses a modular structure with clear separation between services, repositories, models, and the main application logic.

### Project Structure
```
price-comparator-backend/
├── src/
│   ├── main/
│   │   ├── java/com/pricecomparator/
│   │   │   ├── app/           # Main application entry point (App.java)
│   │   │   ├── service/       # Business logic (ValueUnit, BasketOptimizer, etc.)
│   │   │   ├── repository/    # Data access and storage
│   │   │   ├── model/         # Data models (Product, Discount, etc.)
│   │   │   ├── loader/        # Data loading utilities
│   │   │   └── validator/     # Data validation
│   │   └── resources/         # CSV data files
│   └── test/java/com/pricecomparator/service/ # JUnit tests
├── pom.xml                    # Maven build file
├── baskets.json               # Saved baskets
├── output/                    # Output files (e.g., optimized baskets)
```

## Build and Run Instructions

### Prerequisites
- **Java 20** (or Java 17/11) (Java 23+ is not supported for tests)
- **Maven**

### Build
```sh
mvn clean package
```

### Run the Application
```sh
mvn exec:java -Dexec.mainClass="com.pricecomparator.app.App"
```
Or run the generated JAR:
```sh
java -jar target/price-comparator-backend-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Run Tests
```sh
mvn clean test
```

## Assumptions and Simplifications
- Product and discount data are loaded from CSV files in `src/main/resources`.
- The application is CLI-based (no web API by default).
- All price and discount calculations assume data consistency in the CSVs.
- Some features (e.g., alert editing) may require further enhancements for production use.
- Tests use mocks and are designed for logic validation, not full integration.

## How to Use the Features

### CLI Features

- **Basket Optimization:**
  - Optimize your shopping basket for the best prices across stores.
  - **Basket Management:**
    - **Save Basket:** Save your current basket with a custom name for future use.
    - **Load Basket:** Load a previously saved basket.
    - **Update Basket:** Modify a saved basket by adding, removing, or changing product quantities.
    - **Add Products:** Add new products to your current or saved basket.
    - **Clear Basket:** Remove all products from your current basket.
  - The optimized basket split and savings are displayed and saved to the `output/` directory.

- **Best Discounts:**
  - View the top N discounts for a selected store or across all stores for a given date.
  - Discounts are sorted by percentage, and details include product, store, and discount amount.

- **Newest Discounts:**
  - See the latest discounts that have been posted for the selected day.
  - You can filter by store (e.g., Kaufland, Lidl, Profi, or all stores).
  - Useful for quickly spotting new deals as soon as they are available.

- **Price Alerts:**
  - Set alerts for specific products to be notified when their price drops below a target value.
  - Manage alerts: create, view, check, edit, and delete alerts.
  - Alerts are checked against the latest product prices for the selected date.

- **Value per Unit:**
  - Compare products by their price per standard unit (kg, l, buc, etc.).
  - Shows which store offers the best value for a given product ID.
  - Supports conversion for products sold in grams/milliliters to kg/liters.

- **Data Points Analysis:**
  - Analyze historical price data for a specific product.
  - **Filters:**
    - **Category:** Restrict analysis to a specific product category (e.g., Dairy, Fruits).
    - **Brand:** Filter by product brand.
    - **Store:** Filter by store (Kaufland, Lidl, Profi, etc.).
  - **Usage:**
    - Enter a product ID and optionally apply filters.
    - The app displays all available price points for that product, including date, price, store, and any applied discounts.
    - Useful for tracking price trends, comparing brands, or evaluating store pricing strategies.

#### Example CLI Flow
1. Start the app and enter the current date (YYYY-MM-DD).
2. Choose an option from the menu (e.g., Manage basket, Show top discounts).
3. Follow prompts to select products, stores, or enter custom data.
4. Results are displayed in the console and, for some features, saved to the `output/` directory.

### API Endpoints
- **No REST API is implemented by default.**
- If you add endpoints, document them here with example requests and responses.

