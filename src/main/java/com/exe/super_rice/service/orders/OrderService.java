package com.exe.super_rice.service.orders;

import com.exe.super_rice.dto.request.OrderRequestDTO;
import com.exe.super_rice.dto.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getAllOrders();
    OrderResponseDTO updateOrder(Long id, OrderRequestDTO request);
    void deleteOrder(Long id);
}