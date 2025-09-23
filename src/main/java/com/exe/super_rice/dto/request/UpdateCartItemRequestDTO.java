package com.exe.super_rice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCartItemRequestDTO {
    private Long cartItemId;
    private Integer quantity;
}
