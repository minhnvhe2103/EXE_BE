package com.exe.super_rice.service.cart;

import com.exe.super_rice.dto.request.AddToCartRequestDTO;
import com.exe.super_rice.dto.request.UpdateCartItemRequestDTO;
import com.exe.super_rice.dto.response.CartResponseDTO;

public interface CartService {
    CartResponseDTO getCartByUserId(Long userId);
    CartResponseDTO addToCart(Long userId, AddToCartRequestDTO request);
    CartResponseDTO updateCartItem(Long userId, UpdateCartItemRequestDTO request);
    void removeFromCart(Long userId, Long productId);
    void clearCart(Long userId);
    CartResponseDTO checkoutCart(Long userId);
}
