package com.exe.super_rice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDTO {
    private Long id;
    private Long userId;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
