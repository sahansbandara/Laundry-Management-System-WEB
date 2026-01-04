package com.laundry.lms.service;

import com.laundry.lms.model.*;
import com.laundry.lms.repository.PressingCategoryPriceRepository;
import com.laundry.lms.repository.ServiceCatalogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating order prices.
 * Server-side recalculation to prevent client tampering.
 */
@Service
public class PricingService {

    // Base prices (LKR)
    public static final BigDecimal WASH_ONLY_PER_KG = new BigDecimal("250");
    public static final BigDecimal DRY_CLEANING_PER_KG = new BigDecimal("400");
    public static final BigDecimal WASH_IRON_PRESSING_PER_ITEM = new BigDecimal("25");
    public static final BigDecimal PREMIUM_CARE_PER_ITEM = new BigDecimal("400");
    public static final BigDecimal EXPRESS_MULTIPLIER = new BigDecimal("1.25");

    private final PressingCategoryPriceRepository pressingPriceRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;

    public PricingService(PressingCategoryPriceRepository pressingPriceRepository,
            ServiceCatalogRepository serviceCatalogRepository) {
        this.pressingPriceRepository = pressingPriceRepository;
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    /**
     * Calculate subtotal for Laundry (Wash Only) service.
     * 
     * @param weightKg Weight in kilograms
     * @return Subtotal in LKR
     */
    public BigDecimal calculateWashOnly(Double weightKg) {
        if (weightKg == null || weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0");
        }
        return WASH_ONLY_PER_KG.multiply(BigDecimal.valueOf(weightKg))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate subtotal for Dry Cleaning service.
     * 
     * @param weightKg Weight in kilograms
     * @return Subtotal in LKR
     */
    public BigDecimal calculateDryCleaning(Double weightKg) {
        if (weightKg == null || weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0");
        }
        return DRY_CLEANING_PER_KG.multiply(BigDecimal.valueOf(weightKg))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate subtotal for Pressing (Iron Only) service.
     * 
     * @param itemsByCategory Map of PressingCategory -> item count
     * @return Subtotal in LKR
     */
    public BigDecimal calculatePressing(Map<PressingCategory, Integer> itemsByCategory) {
        if (itemsByCategory == null || itemsByCategory.isEmpty()) {
            throw new IllegalArgumentException("At least one item category is required");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<PressingCategory, Integer> entry : itemsByCategory.entrySet()) {
            PressingCategory category = entry.getKey();
            Integer count = entry.getValue();

            if (count != null && count > 0) {
                BigDecimal pricePerItem = getPressingPrice(category);
                total = total.add(pricePerItem.multiply(BigDecimal.valueOf(count)));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get pressing price for a category.
     */
    public BigDecimal getPressingPrice(PressingCategory category) {
        return pressingPriceRepository.findByCategory(category)
                .map(p -> BigDecimal.valueOf(p.getPricePerItem()))
                .orElse(new BigDecimal("50")); // Default price
    }

    /**
     * Calculate subtotal for Wash & Iron service.
     * = Wash price (250 LKR/kg) + Pressing price (25 LKR/item)
     * 
     * @param weightKg  Weight in kilograms
     * @param itemCount Number of items
     * @return Subtotal in LKR
     */
    public BigDecimal calculateWashAndIron(Double weightKg, Integer itemCount) {
        if (weightKg == null || weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0");
        }
        if (itemCount == null || itemCount <= 0) {
            throw new IllegalArgumentException("Item count must be greater than 0");
        }

        BigDecimal washTotal = WASH_ONLY_PER_KG.multiply(BigDecimal.valueOf(weightKg));
        BigDecimal ironTotal = WASH_IRON_PRESSING_PER_ITEM.multiply(BigDecimal.valueOf(itemCount));
        return washTotal.add(ironTotal).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Apply Express Service add-on (+25%).
     * 
     * @param subtotal Current subtotal
     * @return New subtotal with express markup
     */
    public BigDecimal applyExpress(BigDecimal subtotal) {
        return subtotal.multiply(EXPRESS_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Apply Premium Care add-on (+400 LKR per item).
     * 
     * @param subtotal       Current subtotal
     * @param totalItemCount Total number of items across all services
     * @return New subtotal with premium care
     */
    public BigDecimal applyPremiumCare(BigDecimal subtotal, Integer totalItemCount) {
        if (totalItemCount == null || totalItemCount <= 0) {
            throw new IllegalArgumentException("Premium care requires at least one item");
        }
        BigDecimal premiumTotal = PREMIUM_CARE_PER_ITEM.multiply(BigDecimal.valueOf(totalItemCount));
        return subtotal.add(premiumTotal).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get all active pressing category prices.
     */
    public List<PressingCategoryPrice> getActivePressingPrices() {
        return pressingPriceRepository.findByActiveTrue();
    }

    /**
     * Get all active services.
     */
    public List<ServiceCatalog> getActiveServices() {
        return serviceCatalogRepository.findByActiveTrue();
    }
}
