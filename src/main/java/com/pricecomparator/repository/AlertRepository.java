package com.pricecomparator.repository;
import com.pricecomparator.model.PriceAlert;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AlertRepository {
    private List<PriceAlert> alerts;
    private static final String FILE_PATH = "src/main/resources/alerts.csv";

    public AlertRepository() {
        this.alerts = loadAlerts();
    }

    private List<PriceAlert> loadAlerts() {
        List<PriceAlert> alerts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String productId = parts[0];
                    String productName = parts[1];
                    double targetPrice = Double.parseDouble(parts[2]);
                    String userId = parts[3];
                    boolean isActive = parts.length > 4 ? Boolean.parseBoolean(parts[4]) : true;
                    
                    PriceAlert alert = new PriceAlert(productId, productName, targetPrice, userId);
                    alerts.add(alert);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not load alerts file: " + e.getMessage());
        }
        return alerts;
    }
    
    public List<PriceAlert> getActiveAlerts() {
        return alerts;
    }

    public void addAlert(PriceAlert alert) {
        alerts.add(alert);
        saveAlerts();
    }

    public void saveAlerts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (PriceAlert alert : alerts) {
                writer.write(alert.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not save alerts file: " + e.getMessage());
        }
    }

    public void deleteAlert(String productId) {
        alerts.removeIf(alert -> alert.getProductId().equals(productId));
        saveAlerts();
    }

    public void updateAlert(String productId, PriceAlert updatedAlert) {
        int index = alerts.indexOf(new PriceAlert(productId, null, 0, null));
        if (index != -1) {
            alerts.set(index, updatedAlert);
            saveAlerts();
        }
    }

    public void deactivateAlert(String productId) {
        PriceAlert alert = getAlert(productId);
        if (alert != null) {
            alert.setActive(false);
            saveAlerts();
        }
    }

    private PriceAlert getAlert(String productId) {
        return alerts.stream()
            .filter(alert -> alert.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
    }
    

}
