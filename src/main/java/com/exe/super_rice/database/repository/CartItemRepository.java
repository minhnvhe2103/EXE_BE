package com.exe.super_rice.database.repository;

import com.exe.super_rice.database.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    int deleteByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    long countByCartId(@Param("cartId") Long cartId);

    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.product.id = :productId")
    long getTotalQuantityInCartsForProduct(@Param("productId") Long productId);

    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
