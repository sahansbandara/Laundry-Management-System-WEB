package com.laundry.lms.model;

import jakarta.persistence.*;

/**
 * Pressing category prices for iron-only service.
 * Different garment types have different pressing prices.
 */
@Entity
@Table(name = "pressing_category_prices")
public class PressingCategoryPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PressingCategory category;

    @Column(nullable = false)
    private Double pricePerItem;

    @Column(nullable = false)
    private Boolean active = true;

    public PressingCategoryPrice() {
    }

    public PressingCategoryPrice(PressingCategory category, Double pricePerItem) {
        this.category = category;
        this.pricePerItem = pricePerItem;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PressingCategory getCategory() {
        return category;
    }

    public void setCategory(PressingCategory category) {
        this.category = category;
    }

    public Double getPricePerItem() {
        return pricePerItem;
    }

    public void setPricePerItem(Double pricePerItem) {
        this.pricePerItem = pricePerItem;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
