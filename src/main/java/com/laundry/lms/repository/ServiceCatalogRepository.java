package com.laundry.lms.repository;

import com.laundry.lms.model.ServiceCatalog;
import com.laundry.lms.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Long> {
    List<ServiceCatalog> findByActiveTrue();

    Optional<ServiceCatalog> findByServiceType(ServiceType serviceType);
}
