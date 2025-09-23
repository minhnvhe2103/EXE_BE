package com.exe.super_rice.database.repository;

import com.exe.super_rice.database.entity.Product;
import com.exe.super_rice.enums.RiceType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByRiceType(RiceType riceType);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByStockQuantityGreaterThan(Integer quantity);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findAvailableProducts();

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> searchProducts(@Param("keyword") String keyword);

    List<Product> findAllByOrderByPriceAsc();

    List<Product> findAllByOrderByPriceDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    boolean existsByIdAndStockQuantityGreaterThan(Long id, Integer minStock);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    java.util.List<Product> findByIdsWithLock(@Param("ids") java.util.List<Long> ids);
}