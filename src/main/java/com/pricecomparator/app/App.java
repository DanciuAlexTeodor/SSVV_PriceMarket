package com.pricecomparator.app;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.io.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.pricecomparator.service.BasketOptimizer;
import com.pricecomparator.service.BestDiscounts;
import com.pricecomparator.service.NewestDiscounts;
import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.repository.ProductRepository;
import com.pricecomparator.repository.DiscountRepository;
import com.pricecomparator.loader.MarketDataLoader;
import com.pricecomparator.model.Product;
import com.pricecomparator.model.Discount;
import com.pricecomparator.service.PriceDataService;
import com.pricecomparator.service.ValueUnit;

public class App {

    private static final int OPTION_OPTIMIZE_BASKET = 1;
    private static final int OPTION_BEST_DISCOUNTS = 2;
    private static final int OPTION_NEWEST_DISCOUNTS = 3;
    private static final int OPTION_VALUE_PER_UNIT = 4;
    private static final int OPTION_DATA_POINTS_ANALYSIS = 5;
    private static final int OPTION_ADMIN_PANEL = 6;
    private static final int OPTION_EXIT = 0;

    private static final Map<Integer, List<String>> PREDEFINED_BASKETS = new LinkedHashMap<>();
    private static final Map<Integer,String> PREDEFINED_DATES = new LinkedHashMap<>();
    private static final Map<Integer,String> PREDEFINED_STORES = new LinkedHashMap<>();

    private static MarketDataRepository marketDataRepository;
    private static BasketOptimizer basketOptimizer;
    private static BestDiscounts bestDiscounts;
    private static NewestDiscounts newestDiscounts;
    private static ValueUnit valuePerUnit;
    private static String currentDate;
    
    private static final String BASKETS_FILE = "baskets.json";
    private static final Gson gson = new Gson();
    
    static {
        PREDEFINED_BASKETS.put(1, List.of("P001", "P020", "P028", "P034"));
        PREDEFINED_BASKETS.put(2, List.of("P031", "P040", "P008"));
        PREDEFINED_BASKETS.put(3, List.of("P021", "P043", "P026", "P046"));
    }

    static {
        PREDEFINED_DATES.put(1,"2025-05-01");
        PREDEFINED_DATES.put(2,"2025-05-08");
    }

    static {
        PREDEFINED_STORES.put(1,"Kaufland");
        PREDEFINED_STORES.put(2,"Lidl");
        PREDEFINED_STORES.put(3,"Profi");
        PREDEFINED_STORES.put(4,"All stores");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Load baskets from disk
        loadBasketsFromDisk();
        
        // Get today's date at startup
        System.out.println("Welcome to Price Comparator!");
        System.out.println("Press enter to continue");
        currentDate = getDesiredDate(scanner);
        
        if (currentDate == null || currentDate.isEmpty()) {
            System.out.println("Valid date is required to start the application. Exiting...");
            return;
        }
        
        
        // Initialize repositories with the specified date
        initializeRepositories(currentDate);

        while (true) {
            printMenu();

            int choice = readInt(scanner, "Enter your option: ");

            switch (choice) {
                case OPTION_OPTIMIZE_BASKET:
                    handleBasketOptimization(scanner);
                    break;
                case OPTION_BEST_DISCOUNTS:
                    handleBestDiscounts(scanner);
                    break;
                case OPTION_NEWEST_DISCOUNTS:
                    handleNewestDiscounts(scanner);
                    break;
                case OPTION_VALUE_PER_UNIT:
                    handleValuePerUnit(scanner);
                    break;
                case OPTION_DATA_POINTS_ANALYSIS:
                    handleDataPointsAnalysis(scanner);
                    break;
                case OPTION_ADMIN_PANEL:
                    handleAdminPanel(scanner);
                    break;
                case OPTION_EXIT:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
    
    private static void initializeRepositories(String date) {
        // Use the new factory method to create MarketDataRepository from files
        marketDataRepository = MarketDataRepository.createFromFiles();
        
        basketOptimizer = new BasketOptimizer(marketDataRepository);
        bestDiscounts = new BestDiscounts(marketDataRepository);
        newestDiscounts = new NewestDiscounts(marketDataRepository);
        valuePerUnit = new ValueUnit(marketDataRepository);
    }

    private static void printMenu() {
        System.out.println("\n==== Price Comparator Menu ====");
        System.out.println(OPTION_OPTIMIZE_BASKET + ") Manage basket");
        System.out.println(OPTION_BEST_DISCOUNTS + ") Show top discounts");
        System.out.println(OPTION_NEWEST_DISCOUNTS + ") Show newest discounts");
        System.out.println(OPTION_VALUE_PER_UNIT + ") Show best value per unit");
        System.out.println(OPTION_DATA_POINTS_ANALYSIS + ") Show data points for a specific product");
        System.out.println(OPTION_ADMIN_PANEL + ") Admin Panel (Manage Products & Discounts)");
        System.out.println(OPTION_EXIT + ") Exit");
    }

    private static int readInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            scanner.next(); 
            System.out.print("Please enter a valid number: ");
        }
        return scanner.nextInt();
    }

    private static List<String> getDesiredBasket(Scanner scanner)
    {
        System.out.println("\nSelect a basket:");
        PREDEFINED_BASKETS.forEach((key, basket) ->
            System.out.println(key + ") " + basket)
        );
        System.out.println("0) Enter custom product IDs");

        int basketChoice = readInt(scanner, "Your choice: ");
        List<String> basket;

        if (PREDEFINED_BASKETS.containsKey(basketChoice)) {
            return PREDEFINED_BASKETS.get(basketChoice);
        } else if (basketChoice == 0) {
            scanner.nextLine(); // clear newline
            System.out.print("Enter product IDs (comma separated, e.g., P001,P020,P028): ");
            String[] ids = scanner.nextLine().trim().split(",");
            basket = Arrays.asList(ids);
            return basket;
        } else {
            System.out.println("Invalid choice. Returning to main menu.");
            return null;
        }
    }

    private static String getDesiredDate(Scanner scanner) { 
            scanner.nextLine(); 
       
            System.out.print("Enter today's date (YYYY-MM-DD): ");
            String customDate = scanner.nextLine().trim();
            try {
                LocalDate date = LocalDate.parse(customDate);
                LocalDate minDate = LocalDate.parse("2025-05-01");
                LocalDate maxDate = LocalDate.parse("2025-06-01");
                
                if (date.isBefore(minDate)) {
                    System.out.println("Error: Date cannot be before 2025-05-01");
                    return null;
                }
                if (date.isAfter(maxDate)) {
                    System.out.println("Error: Date cannot be after 2025-06-01");
                    return null;
                }
                return customDate;
            } catch (DateTimeParseException e) {
                System.out.println("Error: Invalid date format. Please use YYYY-MM-DD");
                return null;
            }
        
    }

    private static void handleBasketOptimization(Scanner scanner) {
        boolean done = false;
        while (!done) {
            System.out.println("\n==== Basket Management ====");
            System.out.println("1) Optimize basket");
            System.out.println("2) Save basket");
            System.out.println("3) Load basket");
            System.out.println("4) Clear basket");
            System.out.println("5) View all saved baskets");
            System.out.println("6) Modify a saved basket");
            System.out.println("0) Back to main menu");
            int choice = readInt(scanner, "Your choice: ");
            switch (choice) {
                case 1:
                    optimizeBasket(scanner);
                    break;
                case 2:
                    saveBasket(scanner);
                    break;
                case 3:
                    loadBasket(scanner);
                    break;
                case 4:
                    clearBasket();
                    break;
                case 5:
                    viewAllSavedBaskets();
                    break;
                case 6:
                    modifySavedBasket(scanner);
                    break;
                case 0:
                    done = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    
    // Current basket in memory
    private static Map<String, Integer> currentBasket = new LinkedHashMap<>();
    // Map to store saved baskets (basket name -> productId -> quantity)
    private static Map<String, Map<String, Integer>> savedBaskets = new LinkedHashMap<>();
    
    private static void optimizeBasket(Scanner scanner) {
        Map<String, Integer> basket;
        // If there's already a basket in memory, ask if user wants to use it
        if (!currentBasket.isEmpty()) {
            System.out.println("\nCurrent basket: " + currentBasket);
            System.out.println("1) Use current basket");
            System.out.println("2) Select different basket");
            int choice = readInt(scanner, "Your choice: ");
            if (choice == 1) {
                basket = new LinkedHashMap<>(currentBasket);
            } else {
                basket = getDesiredBasketWithQuantities(scanner);
                if (basket != null && !basket.isEmpty()) {
                    currentBasket = new LinkedHashMap<>(basket);
                }
            }
        } else {
            basket = getDesiredBasketWithQuantities(scanner);
            if (basket != null && !basket.isEmpty()) {
                currentBasket = new LinkedHashMap<>(basket);
            }
        }
        if(basket == null || basket.isEmpty()) return;
        // Convert to List<String> for compatibility with optimizer
        List<String> productIds = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : basket.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                productIds.add(entry.getKey());
            }
        }
        basketOptimizer.optimizeBasketSplit(productIds, currentDate);
    }
    
    private static void saveBasket(Scanner scanner) {
        if (currentBasket.isEmpty()) {
            System.out.println("No basket to save. Please create a basket first.");
            return;
        }
        scanner.nextLine(); // Clear scanner buffer
        System.out.print("Enter a name for this basket: ");
        String basketName = scanner.nextLine().trim();
        if (basketName.isEmpty()) {
            System.out.println("Basket name cannot be empty.");
            return;
        }
        savedBaskets.put(basketName, new LinkedHashMap<>(currentBasket));
        saveBasketsToDisk();
        System.out.println("Basket '" + basketName + "' saved successfully.");
    }
    
    private static void loadBasket(Scanner scanner) {
        if (savedBaskets.isEmpty()) {
            System.out.println("No saved baskets found.");
            return;
        }
        System.out.println("\nSaved baskets:");
        int index = 1;
        List<String> basketNames = new ArrayList<>(savedBaskets.keySet());
        for (String name : basketNames) {
            System.out.println(index + ") " + name + ": " + savedBaskets.get(name));
            index++;
        }
        int choice = readInt(scanner, "Select a basket to load (0 to cancel): ");
        if (choice == 0 || choice > basketNames.size()) {
            System.out.println("Operation cancelled or invalid selection.");
            return;
        }
        String selectedName = basketNames.get(choice - 1);
        currentBasket = new LinkedHashMap<>(savedBaskets.get(selectedName));
        System.out.println("Basket '" + selectedName + "' loaded successfully.");
    }
    
    private static void clearBasket() {
        if (currentBasket.isEmpty()) {
            System.out.println("Basket is already empty.");
        } else {
            currentBasket.clear();
            System.out.println("Basket cleared successfully.");
        }
    }

    private static String getDesiredStore(Scanner scanner)
    {
        System.out.println("\nChoose store:");
        

        PREDEFINED_STORES.forEach((key,value) -> 
            System.out.println(key + ")" + value)
        );

        int storeOption = readInt(scanner, "Your choice: ");

        if(PREDEFINED_STORES.containsKey(storeOption)){
            return PREDEFINED_STORES.get(storeOption);
        }
        else{
            System.out.println("Invalid choice. Returning to main menu");
            return null;
        }
    }

    private static void handleNewestDiscounts(Scanner scanner) {
        boolean done = false;
        while (!done) {
            String store = getDesiredStore(scanner);
            if (store == null || store.isEmpty()) return;
            newestDiscounts.showNewestDiscounts(store, currentDate);
            System.out.println("\nPress Enter to continue or type '0' to return to main menu.");
            scanner.nextLine();
            String input = scanner.nextLine();
            if (input.trim().equals("0")) done = true;
        }
    }

    private static void handleBestDiscounts(Scanner scanner) {
        boolean done = false;
        while (!done) {
            String store = getDesiredStore(scanner);
            if (store == null || store.isEmpty()) return;
            int numberOfOffers = readInt(scanner, "Enter number of offers (0 to go back):");
            if (numberOfOffers == 0) {
                done = true;
                continue;
            }
            bestDiscounts.showBestDiscounts(store, currentDate, numberOfOffers);
            System.out.println("\nPress Enter to continue or type '0' to return to main menu.");
            scanner.nextLine();
            String input = scanner.nextLine();
            if (input.trim().equals("0")) done = true;
        }
    }

    private static void handleValuePerUnit(Scanner scanner) {
        boolean done = false;
        while (!done) {
            scanner.nextLine();
            System.out.print("Enter product ID (or 0 to go back): ");
            String productId = scanner.nextLine().trim();
            if (productId.equals("0")) {
                done = true;
                continue;
            }
            valuePerUnit.getBestValuePerUnit(productId, currentDate);
            System.out.println("\nPress Enter to continue or type '0' to return to main menu.");
            String input = scanner.nextLine();
            if (input.trim().equals("0")) done = true;
        }
    }

    private static void handleDataPointsAnalysis(Scanner scanner) {
        boolean done = false;
        while (!done) {
            System.out.println("\n==== Data Points Analysis ====");
            System.out.println("1) Show data points for a specific product");
            System.out.println("0) Back to main menu");
            int choice = readInt(scanner, "Your choice: ");
            switch (choice) {
                case 1:
                    showDataPointsForProduct(scanner);
                    break;
                case 0:
                    done = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void showDataPointsForProduct(Scanner scanner) {
        scanner.nextLine();
        System.out.print("Enter product ID: ");
        String productId = scanner.nextLine().trim();
        
        // Initialize filters as null
        String categoryFilter = null;
        String brandFilter = null;
        String storeFilter = null;
        
        // Ask for filters
        System.out.println("\nDo you want to apply filters? (Y/N)");
        String filterChoice = scanner.nextLine().trim().toUpperCase();
        
        if (filterChoice.equals("Y")) {
            System.out.println("Enter filter values (leave blank for no filter):");
            
            System.out.print("Category filter: ");
            String category = scanner.nextLine().trim();
            if (!category.isEmpty()) {
                categoryFilter = category;
            }
            
            System.out.print("Brand filter: ");
            String brand = scanner.nextLine().trim();
            if (!brand.isEmpty()) {
                brandFilter = brand;
            }
            
            System.out.print("Store filter: ");
            String store = scanner.nextLine().trim();
            if (!store.isEmpty()) {
                storeFilter = store;
            }
        }
        
        // Create service and show data points with optional filters
        PriceDataService priceDataService = new PriceDataService();
        priceDataService.showDataPointsForProduct(productId, currentDate, categoryFilter, brandFilter, storeFilter);
    }

    private static void viewAllSavedBaskets() {
        if (savedBaskets.isEmpty()) {
            System.out.println("No saved baskets.");
            return;
        }
        System.out.println("\n==== Saved Baskets ====");
        for (Map.Entry<String, Map<String, Integer>> entry : savedBaskets.entrySet()) {
            System.out.println("Basket: " + entry.getKey());
            for (Map.Entry<String, Integer> prod : entry.getValue().entrySet()) {
                System.out.println("  Product ID: " + prod.getKey() + " | Quantity: " + prod.getValue());
            }
        }
    }

    private static void modifySavedBasket(Scanner scanner) {
        if (savedBaskets.isEmpty()) {
            System.out.println("No saved baskets to modify.");
            return;
        }
        System.out.println("\n==== Modify Saved Basket ====");
        List<String> basketNames = new ArrayList<>(savedBaskets.keySet());
        for (int i = 0; i < basketNames.size(); i++) {
            System.out.println((i+1) + ") " + basketNames.get(i));
        }
        int choice = readInt(scanner, "Select a basket to modify (0 to cancel): ");
        if (choice == 0 || choice > basketNames.size()) {
            System.out.println("Operation cancelled or invalid selection.");
            return;
        }
        String selectedName = basketNames.get(choice - 1);
        Map<String, Integer> basket = savedBaskets.get(selectedName);
        boolean done = false;
        while (!done) {
            System.out.println("\nEditing basket: " + selectedName);
            for (Map.Entry<String, Integer> prod : basket.entrySet()) {
                System.out.println("  Product ID: " + prod.getKey() + " | Quantity: " + prod.getValue());
            }
            System.out.println("1) Add product");
            System.out.println("2) Delete product");
            System.out.println("3) Edit product quantity");
            System.out.println("0) Done");
            int op = readInt(scanner, "Your choice: ");
            switch (op) {
                case 1:
                    scanner.nextLine();
                    System.out.print("Enter product ID to add: ");
                    String addId = scanner.nextLine().trim();
                    System.out.print("Enter quantity: ");
                    int addQty = readInt(scanner, "");
                    basket.put(addId, basket.getOrDefault(addId, 0) + addQty);
                    System.out.println("Product added/updated.");
                    saveBasketsToDisk();
                    break;
                case 2:
                    scanner.nextLine();
                    System.out.print("Enter product ID to delete: ");
                    String delId = scanner.nextLine().trim();
                    if (basket.containsKey(delId)) {
                        basket.remove(delId);
                        System.out.println("Product removed.");
                        saveBasketsToDisk();
                    } else {
                        System.out.println("Product not found in basket.");
                    }
                    break;
                case 3:
                    scanner.nextLine();
                    System.out.print("Enter product ID to edit: ");
                    String editId = scanner.nextLine().trim();
                    if (basket.containsKey(editId)) {
                        System.out.print("Enter new quantity: ");
                        int newQty = readInt(scanner, "");
                        if (newQty > 0) {
                            basket.put(editId, newQty);
                            System.out.println("Quantity updated.");
                        } else {
                            basket.remove(editId);
                            System.out.println("Product removed (quantity set to 0).");
                        }
                        saveBasketsToDisk();
                    } else {
                        System.out.println("Product not found in basket.");
                    }
                    break;
                case 0:
                    done = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
        System.out.println("Basket '" + selectedName + "' updated.");
    }

    // Helper to get basket with quantities from user
    private static Map<String, Integer> getDesiredBasketWithQuantities(Scanner scanner) {
        System.out.println("\nSelect a basket:");
        PREDEFINED_BASKETS.forEach((key, basket) ->
            System.out.println(key + ") " + basket)
        );
        System.out.println("0) Enter custom product IDs");
        int basketChoice = readInt(scanner, "Your choice: ");
        Map<String, Integer> basket = new LinkedHashMap<>();
        if (PREDEFINED_BASKETS.containsKey(basketChoice)) {
            for (String pid : PREDEFINED_BASKETS.get(basketChoice)) {
                basket.put(pid, basket.getOrDefault(pid, 0) + 1);
            }
            return basket;
        } else if (basketChoice == 0) {
            scanner.nextLine(); // clear newline
            System.out.print("Enter product IDs and quantities (e.g., P001:2,P020:1): ");
            String[] entries = scanner.nextLine().trim().split(",");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String pid = parts[0].trim();
                    int qty = 1;
                    try {
                        qty = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid quantity for " + pid + ", defaulting to 1.");
                    }
                    basket.put(pid, basket.getOrDefault(pid, 0) + qty);
                } else if (parts.length == 1) {
                    String pid = parts[0].trim();
                    basket.put(pid, basket.getOrDefault(pid, 0) + 1);
                }
            }
            return basket;
        } else {
            System.out.println("Invalid choice. Returning to main menu.");
            return null;
        }
    }

    private static void loadBasketsFromDisk() {
        File file = new File(BASKETS_FILE);
        if (!file.exists()) return;
        try (Reader reader = new FileReader(file)) {
            java.lang.reflect.Type type = new TypeToken<Map<String, Map<String, Integer>>>(){}.getType();
            Map<String, Map<String, Integer>> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                savedBaskets.clear();
                savedBaskets.putAll(loaded);
            }
        } catch (IOException e) {
            System.out.println("Failed to load baskets from disk: " + e.getMessage());
        }
    }

    private static void saveBasketsToDisk() {
        try (Writer writer = new FileWriter(BASKETS_FILE)) {
            gson.toJson(savedBaskets, writer);
        } catch (IOException e) {
            System.out.println("Failed to save baskets to disk: " + e.getMessage());
        }
    }

    private static void handleAdminPanel(Scanner scanner) {
        boolean done = false;
        while (!done) {
            System.out.println("\n==== Admin Panel ====");
            System.out.println("1) Manage Products");
            System.out.println("2) Manage Discounts");
            System.out.println("0) Back to main menu");
            int choice = readInt(scanner, "Your choice: ");
            switch (choice) {
                case 1:
                    handleManageProducts(scanner);
                    break;
                case 2:
                    handleManageDiscounts(scanner);
                    break;
                case 0:
                    done = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void handleManageProducts(Scanner scanner) {
        System.out.println("\n--- Manage Products ---");
        System.out.println("1) Add Product");
        System.out.println("2) Edit Product Price");
        System.out.println("3) Delete Product");
        System.out.println("0) Back");
        int choice = readInt(scanner, "Choice: ");
        if (choice == 0) return;

        String store = getDesiredStore(scanner);
        if (store == null || store.equals("All stores")) {
            System.out.println("Please select a specific store.");
            return;
        }

        scanner.nextLine(); // clear buffer
        System.out.print("Enter Product ID: ");
        String id = scanner.nextLine().trim();

        if (choice == 1) {
            System.out.print("Name: "); String name = scanner.nextLine().trim();
            System.out.print("Category: "); String cat = scanner.nextLine().trim();
            System.out.print("Brand: "); String brand = scanner.nextLine().trim();
            System.out.print("Quantity: "); double qty = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Unit (kg, l, buc, etc.): "); String unit = scanner.nextLine().trim();
            System.out.print("Price: "); double price = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Currency (RON): "); String curr = scanner.nextLine().trim();
            
            Product p = new Product(id, name, cat, brand, qty, unit, price, curr, currentDate);
            marketDataRepository.addProduct(store, currentDate, p);
            System.out.println("Product added successfully!");
        } else if (choice == 2) {
            Product p = marketDataRepository.getProduct(store, id, currentDate);
            if (p == null) { System.out.println("Product not found!"); return; }
            System.out.print("New Price (current: " + p.getPrice() + "): ");
            double price = Double.parseDouble(scanner.nextLine().trim());
            Product updated = new Product(id, p.getName(), p.getCategory(), p.getBrand(), p.getQuantity(), p.getUnit(), price, p.getCurrency(), currentDate);
            marketDataRepository.updateProduct(store, currentDate, id, updated);
            System.out.println("Product updated successfully!");
        } else if (choice == 3) {
            marketDataRepository.deleteProduct(store, currentDate, id);
            System.out.println("Product deleted successfully!");
        }
    }

    private static void handleManageDiscounts(Scanner scanner) {
        System.out.println("\n--- Manage Discounts ---");
        System.out.println("1) Add Discount");
        System.out.println("2) Edit Discount Percentage");
        System.out.println("3) Delete Discount");
        System.out.println("0) Back");
        int choice = readInt(scanner, "Choice: ");
        if (choice == 0) return;

        String store = getDesiredStore(scanner);
        if (store == null || store.equals("All stores")) {
            System.out.println("Please select a specific store.");
            return;
        }

        scanner.nextLine(); // clear buffer
        System.out.print("Enter Product ID: ");
        String id = scanner.nextLine().trim();

        if (choice == 1) {
            System.out.print("Product Name: "); String name = scanner.nextLine().trim();
            System.out.print("Brand: "); String brand = scanner.nextLine().trim();
            System.out.print("Quantity: "); String qty = scanner.nextLine().trim();
            System.out.print("Unit: "); String unit = scanner.nextLine().trim();
            System.out.print("Category: "); String cat = scanner.nextLine().trim();
            System.out.print("From Date (YYYY-MM-DD): "); String from = scanner.nextLine().trim();
            System.out.print("To Date (YYYY-MM-DD): "); String to = scanner.nextLine().trim();
            System.out.print("Discount %: "); int pct = Integer.parseInt(scanner.nextLine().trim());

            Discount d = new Discount(id, name, brand, qty, unit, cat, from, to, pct, currentDate);
            marketDataRepository.addDiscount(store, currentDate, d);
            System.out.println("Discount added successfully!");
        } else if (choice == 2) {
            Discount d = marketDataRepository.getActiveDiscount(store, id, currentDate);
            if (d == null) { System.out.println("Active discount not found!"); return; }
            System.out.print("New Discount % (current: " + d.getDiscountPercent() + "): ");
            int pct = Integer.parseInt(scanner.nextLine().trim());
            Discount updated = new Discount(id, d.getProductName(), d.getBrand(), d.getQuantity(), d.getUnit(), d.getCategory(), d.getFromDate(), d.getToDate(), pct, currentDate);
            marketDataRepository.updateDiscount(store, currentDate, id, updated);
            System.out.println("Discount updated successfully!");
        } else if (choice == 3) {
            marketDataRepository.deleteDiscount(store, currentDate, id);
            System.out.println("Discount deleted successfully!");
        }
    }
}
