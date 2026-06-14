package com.server.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrestamoResponseDto {
    private Long id;
    private BigDecimal capitalSolicitado;
    private BigDecimal tasaInteresAnual;
    private Integer plazoMeses;
    private String estado;
    private UserResponseDto usuario;
    private List<PlanPagoResponseDto> planPago;
}
