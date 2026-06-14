package com.server.app.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Table(name = "prestamos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capital_solicitado")
    private BigDecimal capitalSolicitado;

    @Column(name = "tasa_interes_anual")
    private BigDecimal tasaInteresAnual;

    @Column(name = "plazo_meses")
    private Integer plazoMeses;

    @Column(length = 20)
    private String estado; // APROBADO, PENDIENTE, PAGADO

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private User usuario;
}
