package com.exe.super_rice.controller;

import com.exe.super_rice.dto.request.OrderRequestDTO;
import com.exe.super_rice.dto.response.ApiResponse;
import com.exe.super_rice.dto.response.OrderResponseDTO;
import com.exe.super_rice.service.orders.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) {
        return ApiResponse.<OrderResponseDTO>builder()
                .message("Order created successfully")
                .data(orderService.createOrder(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponseDTO> getOrder(@PathVariable Long id) {
        return ApiResponse.<OrderResponseDTO>builder()
                .message("Order fetched successfully")
                .data(orderService.getOrderById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrderResponseDTO>> getAllOrders() {
        return ApiResponse.<List<OrderResponseDTO>>builder()
                .message("Orders fetched successfully")
                .data(orderService.getAllOrders())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderResponseDTO> updateOrder(@PathVariable Long id, @RequestBody OrderRequestDTO request) {
        return ApiResponse.<OrderResponseDTO>builder()
                .message("Order updated successfully")
                .data(orderService.updateOrder(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.<Void>builder()
                .message("Order deleted successfully")
                .build();
    }
}
