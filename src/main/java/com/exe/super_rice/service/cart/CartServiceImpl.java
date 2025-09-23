package com.exe.super_rice.service.cart;

import com.exe.super_rice.database.entity.*;
import com.exe.super_rice.database.repository.*;
import com.exe.super_rice.dto.request.AddToCartRequestDTO;
import com.exe.super_rice.dto.request.UpdateCartItemRequestDTO;
import com.exe.super_rice.dto.response.CartItemResponseDTO;
import com.exe.super_rice.dto.response.CartResponseDTO;
import com.exe.super_rice.enums.ErrorCode;
import com.exe.super_rice.exception.AppException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    OrderRepository orderRepository;

    private CartResponseDTO mapToCartResponse(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = items.stream()
                .mapToInt(CartItemResponseDTO::getQuantity)
                .sum();

        return CartResponseDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponseDTO mapToCartItemResponse(CartItem item) {
        return CartItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productDescription(item.getProduct().getDescription())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .unit(item.getProduct().getUnit())
                .build();
    }

    @Override
    @Transactional
    public CartResponseDTO getCartByUserId(Long userId) {
        try {
            Users user = getUserById(userId);
            Optional<Cart> existingCart = cartRepository.findByUserIdWithItems(userId);

            Cart cart = existingCart.orElseGet(() -> createNewCartSafely(user));
            
            return mapToCartResponse(cart);
        } catch (Exception e) {
            log.error("Error getting cart for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to get cart", e);
        }
    }

    private Users getUserById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    private Cart createNewCart(Users user) {
        Cart cart = Cart.builder()
                .user(user)
                .build();
        return cartRepository.save(cart);
    }

    @Transactional
    protected Cart createNewCartSafely(Users user) {
        try {
            Optional<Cart> existingCart = cartRepository.findByUser(user);
            if (existingCart.isPresent()) {
                log.debug("Cart already exists for user {}, returning existing cart", user.getId());
                return existingCart.get();
            }

            Cart cart = Cart.builder()
                    .user(user)
                    .build();

            Cart savedCart = cartRepository.save(cart);
            log.info("Created new cart {} for user {}", savedCart.getId(), user.getId());
            return savedCart;

        } catch (DataIntegrityViolationException e) {
            log.warn("Cart already exists for user {} due to concurrent creation, fetching existing cart", user.getId());
            return cartRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Failed to create or find cart for user: " + user.getId()));
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Retryable(value = {OptimisticLockException.class, DataIntegrityViolationException.class},
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public CartResponseDTO addToCart(Long userId, AddToCartRequestDTO request) {
        try {
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                throw new AppException(ErrorCode.INVALID_PRODUCT_REQUEST);
            }

            Users user = getUserById(userId);

            Product product = productRepository.findByIdWithLock(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            if (product.getStockQuantity() < request.getQuantity()) {
                log.warn("Insufficient stock for product {}: requested={}, available={}",
                        product.getId(), request.getQuantity(), product.getStockQuantity());
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }

            Cart cart = getOrCreateCart(user);

            Optional<CartItem> existingItem = cartItemRepository
                    .findByCartIdAndProductId(cart.getId(), product.getId());

            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity() + request.getQuantity();

                if (product.getStockQuantity() < newQuantity) {
                    throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                }

                item.setQuantity(newQuantity);
                cartItemRepository.save(item);

                log.info("Updated cart item quantity for user {}, product {}: {} -> {}",
                        userId, product.getId(), item.getQuantity() - request.getQuantity(), newQuantity);
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(request.getQuantity())
                        .price(product.getPrice())
                        .build();

                cartItemRepository.save(newItem);

                log.info("Added new item to cart for user {}, product {}: quantity={}",
                        userId, product.getId(), request.getQuantity());
            }

            cart = cartRepository.findById(cart.getId())
                    .orElseThrow(() -> new RuntimeException("Cart not found after update"));

            return mapToCartResponse(cart);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding to cart for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to add item to cart", e);
        }
    }

    private Cart getOrCreateCart(Users user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> createNewCart(user));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CartResponseDTO updateCartItem(Long userId, UpdateCartItemRequestDTO request) {
        try {
            if (request.getQuantity() == null || request.getQuantity() < 0) {
                throw new AppException(ErrorCode.INVALID_PRODUCT_REQUEST);
            }

            Users user = getUserById(userId);

            CartItem cartItem = cartItemRepository.findById(request.getCartItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

            if (!cartItem.getCart().getUser().getId().equals(userId)) {
                log.warn("Unauthorized cart access attempt: user {} trying to access cart item {}",
                        userId, request.getCartItemId());
                throw new AppException(ErrorCode.UNAUTHORIZED_CART_ACCESS);
            }

            if (request.getQuantity() == 0) {
                cartItemRepository.delete(cartItem);
                log.info("Removed cart item {} for user {}", request.getCartItemId(), userId);
            } else {
                Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

                if (product.getStockQuantity() < request.getQuantity()) {
                    throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                }

                int oldQuantity = cartItem.getQuantity();
                cartItem.setQuantity(request.getQuantity());
                cartItemRepository.save(cartItem);

                log.info("Updated cart item {} for user {}: quantity {} -> {}",
                        request.getCartItemId(), userId, oldQuantity, request.getQuantity());
            }

            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            return mapToCartResponse(cart);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating cart item for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update cart item", e);
        }
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        try {
            Users user = getUserById(userId);

            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

            int deletedCount = cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);

            if (deletedCount > 0) {
                log.info("Removed product {} from cart for user {}", productId, userId);
            } else {
                log.warn("Attempted to remove non-existent product {} from cart for user {}", productId, userId);
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing from cart for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to remove item from cart", e);
        }
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        try {
            Users user = getUserById(userId);

            cartRepository.findByUser(user).ifPresent(cart -> {
                int itemCount = cart.getItems().size();
                cart.clearItems();
                cartRepository.save(cart);
                log.info("Cleared {} items from cart for user {}", itemCount, userId);
            });

        } catch (Exception e) {
            log.error("Error clearing cart for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to clear cart", e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(value = {OptimisticLockException.class}, maxAttempts = 3, backoff = @Backoff(delay = 200))
    public CartResponseDTO checkoutCart(Long userId) {
        try {
            Users user = getUserById(userId);

            Cart cart = cartRepository.findByUserIdWithItems(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

            if (cart.getItems().isEmpty()) {
                throw new AppException(ErrorCode.CART_IS_EMPTY);
            }

            Order order = new Order();
            order.setUser(user);

            BigDecimal totalAmount = BigDecimal.ZERO;

            for (CartItem cartItem : cart.getItems()) {
                Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    log.error("Insufficient stock during checkout for product {}: requested={}, available={}",
                            product.getId(), cartItem.getQuantity(), product.getStockQuantity());
                    throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getPrice());

                order.getItems().add(orderItem);
                totalAmount = totalAmount.add(cartItem.getSubtotal());

                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);

                log.info("Processed checkout item: product={}, quantity={}, remaining_stock={}",
                        product.getId(), cartItem.getQuantity(), product.getStockQuantity());
            }

            order.setTotalAmount(totalAmount);
            Order savedOrder = orderRepository.save(order);

            CartResponseDTO response = mapToCartResponse(cart);

            cart.clearItems();
            cartRepository.save(cart);

            log.info("Successfully checked out cart for user {}, created order {}, total amount: {}",
                    userId, savedOrder.getId(), totalAmount);

            return response;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during checkout for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Checkout failed", e);
        }
    }
}
