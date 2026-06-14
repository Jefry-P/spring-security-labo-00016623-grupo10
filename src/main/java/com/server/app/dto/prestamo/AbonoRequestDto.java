package com.server.app.dto.prestamo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AbonoRequestDto {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
    private BigDecimal monto;

    private BigDecimal recargoMora; // Optional, defaults to zero

    @NotNull(message = "El ID de plan de pago es obligatorio")
    private Long planPagoId;
}
