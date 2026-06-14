package com.server.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanPagoResponseDto {
    private Long id;
    private Integer numeroCuota;
    private BigDecimal montoCapital;
    private BigDecimal montoInteres;
    private LocalDate fechaVencimiento;
    private String estado;
}
