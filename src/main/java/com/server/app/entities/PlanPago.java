package com.server.app.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "planes_pago")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class PlanPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    @Column(name = "monto_capital")
    private BigDecimal montoCapital;

    @Column(name = "monto_interes")
    private BigDecimal montoInteres;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(length = 20)
    private String estado; // PENDIENTE, PAGADO

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prestamo_id")
    private Prestamo prestamo;
}
