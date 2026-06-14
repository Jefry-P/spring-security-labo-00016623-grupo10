package com.server.app.mappers;

import com.server.app.dto.response.AbonoResponseDto;
import com.server.app.dto.response.PlanPagoResponseDto;
import com.server.app.dto.response.PrestamoResponseDto;
import com.server.app.entities.Abono;
import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrestamoMapper {

    public static PrestamoResponseDto toPrestamoResponseDto(Prestamo prestamo, List<PlanPago> planPagoList) {
        if (prestamo == null) {
            return null;
        }
        return PrestamoResponseDto.builder()
                .id(prestamo.getId())
                .capitalSolicitado(prestamo.getCapitalSolicitado())
                .tasaInteresAnual(prestamo.getTasaInteresAnual())
                .plazoMeses(prestamo.getPlazoMeses())
                .estado(prestamo.getEstado())
                .usuario(UserMapper.toUserResponseDto(prestamo.getUsuario()))
                .planPago(planPagoList != null ? planPagoList.stream()
                        .map(PrestamoMapper::toPlanPagoResponseDto)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    public static PlanPagoResponseDto toPlanPagoResponseDto(PlanPago planPago) {
        if (planPago == null) {
            return null;
        }
        return PlanPagoResponseDto.builder()
                .id(planPago.getId())
                .numeroCuota(planPago.getNumeroCuota())
                .montoCapital(planPago.getMontoCapital())
                .montoInteres(planPago.getMontoInteres())
                .fechaVencimiento(planPago.getFechaVencimiento())
                .estado(planPago.getEstado())
                .build();
    }

    public static AbonoResponseDto toAbonoResponseDto(Abono abono) {
        if (abono == null) {
            return null;
        }
        return AbonoResponseDto.builder()
                .id(abono.getId())
                .monto(abono.getMonto())
                .fechaPago(abono.getFechaPago())
                .recargoMora(abono.getRecargoMora())
                .planPagoId(abono.getPlanPago() != null ? abono.getPlanPago().getId() : null)
                .build();
    }
}
