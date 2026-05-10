package com.pricecomparator;

import com.pricecomparator.repository.MarketDataRepository;
import com.pricecomparator.service.PriceDataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public MarketDataRepository marketDataRepository() {
        return MarketDataRepository.createFromFiles();
    }

    @Bean
    public PriceDataService priceDataService(MarketDataRepository marketDataRepository) {
        return new PriceDataService(marketDataRepository);
    }
}