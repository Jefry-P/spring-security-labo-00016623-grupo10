package com.server.app.dto.prestamo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PrestamoRequestDto {

    @NotNull(message = "El capital solicitado es obligatorio")
    @DecimalMin(value = "0.01", message = "El capital solicitado debe ser mayor que cero")
    private BigDecimal capitalSolicitado;

    @NotNull(message = "La tasa de interés anual es obligatoria")
    @DecimalMin(value = "0.01", message = "La tasa de interés anual debe ser mayor que cero")
    private BigDecimal tasaInteresAnual;

    @NotNull(message = "El plazo en meses es obligatorio")
    @Min(value = 1, message = "El plazo en meses debe ser al menos 1")
    private Integer plazoMeses;

    private String estado; // APROBADO, PENDIENTE, PAGADO

    private Integer usuarioId; // Optional, can fallback to current logged-in user
}
