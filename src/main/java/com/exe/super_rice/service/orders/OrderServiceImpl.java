package com.exe.super_rice.service.orders;

import com.exe.super_rice.database.entity.Order;
import com.exe.super_rice.database.entity.OrderItem;
import com.exe.super_rice.database.entity.Product;
import com.exe.super_rice.database.entity.Users;
import com.exe.super_rice.database.repository.OrderRepository;
import com.exe.super_rice.database.repository.ProductRepository;
import com.exe.super_rice.database.repository.UserRepository;
import com.exe.super_rice.dto.request.OrderRequestDTO;
import com.exe.super_rice.dto.response.OrderItemResponseDTO;
import com.exe.super_rice.dto.response.OrderResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(request.getTotalAmount());

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            return item;
        }).collect(Collectors.toList());

        order.setItems(items);
        Order saved = orderRepository.save(order);

        return mapToResponse(saved);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        order.setTotalAmount(request.getTotalAmount());

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            return item;
        }).toList();

        order.getItems().clear();
        order.getItems().addAll(items);

        Order updated = orderRepository.save(order);
        return mapToResponse(updated);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    private OrderResponseDTO mapToResponse(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt().atStartOfDay())
                .items(order.getItems().stream().map(item ->
                        OrderItemResponseDTO.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}