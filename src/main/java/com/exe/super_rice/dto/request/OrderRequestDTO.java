package com.exe.super_rice.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequestDTO {
    private Long userId;
    private BigDecimal totalAmount;
    private List<OrderItemRequestDTO> items;
}

