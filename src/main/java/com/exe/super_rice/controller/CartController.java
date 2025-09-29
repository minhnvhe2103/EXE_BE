package com.exe.super_rice.controller;

import com.exe.super_rice.dto.request.AddToCartRequestDTO;
import com.exe.super_rice.dto.request.UpdateCartItemRequestDTO;
import com.exe.super_rice.dto.response.ApiResponse;
import com.exe.super_rice.dto.response.CartResponseDTO;
import com.exe.super_rice.service.cart.CartService;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.ParseException;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {

    CartService cartService;

    @GetMapping
    public ApiResponse<CartResponseDTO> getCart() {
        Long userId = getCurrentUserId();
        return ApiResponse.<CartResponseDTO>builder()
                .message("Cart fetched successfully")
                .data(cartService.getCartByUserId(userId))
                .build();
    }

    @PostMapping("/add")
    public ApiResponse<CartResponseDTO> addToCart(@RequestBody AddToCartRequestDTO request) {
        Long userId = getCurrentUserId();
        return ApiResponse.<CartResponseDTO>builder()
                .message("Product added to cart successfully")
                .data(cartService.addToCart(userId, request))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<CartResponseDTO> updateCartItem(@RequestBody UpdateCartItemRequestDTO request) {
        Long userId = getCurrentUserId();
        return ApiResponse.<CartResponseDTO>builder()
                .message("Cart item updated successfully")
                .data(cartService.updateCartItem(userId, request))
                .build();
    }

    @DeleteMapping("/remove/{productId}")
    public ApiResponse<Void> removeFromCart(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        cartService.removeFromCart(userId, productId);
        return ApiResponse.<Void>builder()
                .message("Product removed from cart successfully")
                .build();
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clearCart() {
        Long userId = getCurrentUserId();
        cartService.clearCart(userId);
        return ApiResponse.<Void>builder()
                .message("Cart cleared successfully")
                .build();
    }

    @PostMapping("/checkout")
    public ApiResponse<CartResponseDTO> checkoutCart() {
        Long userId = getCurrentUserId();
        return ApiResponse.<CartResponseDTO>builder()
                .message("Cart checked out successfully")
                .data(cartService.checkoutCart(userId))
                .build();
    }

    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Authorization token is missing or invalid format");
            }

            String token = authHeader.substring(7);

            SignedJWT signedJWT = SignedJWT.parse(token);

            Object userIdClaim = signedJWT.getJWTClaimsSet().getClaim("id");

            if (userIdClaim == null) {
                throw new RuntimeException("User ID not found in token");
            }

            Long userId;
            if (userIdClaim instanceof Number) {
                userId = ((Number) userIdClaim).longValue();
            } else {
                userId = Long.parseLong(userIdClaim.toString());
            }

            log.debug("Extracted user ID {} from JWT token", userId);
            return userId;

        } catch (ParseException e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token format", e);
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            throw new RuntimeException("Failed to authenticate user", e);
        }
    }
}
