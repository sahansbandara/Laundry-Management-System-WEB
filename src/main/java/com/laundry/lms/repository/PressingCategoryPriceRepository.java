package com.laundry.lms.repository;

import com.laundry.lms.model.PressingCategory;
import com.laundry.lms.model.PressingCategoryPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PressingCategoryPriceRepository extends JpaRepository<PressingCategoryPrice, Long> {
    List<PressingCategoryPrice> findByActiveTrue();

    Optional<PressingCategoryPrice> findByCategory(PressingCategory category);
}
