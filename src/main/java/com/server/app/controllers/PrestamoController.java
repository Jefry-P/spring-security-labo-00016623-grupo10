package com.server.app.controllers;

import com.server.app.dto.prestamo.AbonoRequestDto;
import com.server.app.dto.prestamo.PrestamoRequestDto;
import com.server.app.dto.response.*;
import com.server.app.entities.Abono;
import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.mappers.PrestamoMapper;
import com.server.app.services.PrestamoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prestamos")
@AllArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @PostMapping
    public ResponseEntity<PrestamoResponseDto> create(@Valid @RequestBody PrestamoRequestDto dto) {
        Prestamo prestamo = prestamoService.createPrestamo(dto);
        List<PlanPago> plan = prestamoService.findPlanPagoByPrestamoId(prestamo.getId());
        return ResponseEntity.ok(PrestamoMapper.toPrestamoResponseDto(prestamo, plan));
    }

    @GetMapping
    public ResponseEntity<Pagination<PrestamoResponseDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Prestamo> prestamosPage = prestamoService.findAll(page, size);
        List<PrestamoResponseDto> content = prestamosPage.getContent().stream()
                .map(prestamo -> {
                    List<PlanPago> plan = prestamoService.findPlanPagoByPrestamoId(prestamo.getId());
                    return PrestamoMapper.toPrestamoResponseDto(prestamo, plan);
                })
                .collect(Collectors.toList());

        Pagination<PrestamoResponseDto> pagination = new Pagination<>(
                content,
                new PaginationMeta(
                        prestamosPage.getNumber(),
                        prestamosPage.getSize(),
                        prestamosPage.getTotalPages(),
                        prestamosPage.getTotalElements()
                )
        );

        return ResponseEntity.ok(pagination);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponseDto> findById(@PathVariable Long id) {
        Prestamo prestamo = prestamoService.findById(id);
        List<PlanPago> plan = prestamoService.findPlanPagoByPrestamoId(prestamo.getId());
        return ResponseEntity.ok(PrestamoMapper.toPrestamoResponseDto(prestamo, plan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrestamoResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PrestamoRequestDto dto
    ) {
        Prestamo prestamo = prestamoService.update(id, dto);
        List<PlanPago> plan = prestamoService.findPlanPagoByPrestamoId(prestamo.getId());
        return ResponseEntity.ok(PrestamoMapper.toPrestamoResponseDto(prestamo, plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        prestamoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/abonos")
    public ResponseEntity<AbonoResponseDto> pay(
            @PathVariable Long id,
            @Valid @RequestBody AbonoRequestDto dto
    ) {
        // Enforce matching route parameter ID to requested planPago prestamo relation internally or directly
        Abono abono = prestamoService.createAbono(dto);
        return ResponseEntity.ok(PrestamoMapper.toAbonoResponseDto(abono));
    }
}
