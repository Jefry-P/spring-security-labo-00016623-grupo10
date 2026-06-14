package com.server.app.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "abonos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Abono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "recargo_mora", precision = 15, scale = 2)
    private BigDecimal recargoMora;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_pago_id")
    private PlanPago planPago;
}
