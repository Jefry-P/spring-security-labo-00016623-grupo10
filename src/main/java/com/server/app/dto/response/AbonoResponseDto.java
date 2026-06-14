package com.server.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AbonoResponseDto {
    private Long id;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private BigDecimal recargoMora;
    private Long planPagoId;
}
