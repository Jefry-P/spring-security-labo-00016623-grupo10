package com.server.app.services;

import com.server.app.dto.prestamo.AbonoRequestDto;
import com.server.app.dto.prestamo.PrestamoRequestDto;
import com.server.app.entities.Abono;
import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.entities.User;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.AbonoRepository;
import com.server.app.repositories.PlanPagoRepository;
import com.server.app.repositories.PrestamoRepository;
import com.server.app.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final PlanPagoRepository planPagoRepository;
    private final AbonoRepository abonoRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional
    public Prestamo createPrestamo(PrestamoRequestDto dto) {
        User user;
        if (dto.getUsuarioId() != null) {
            user = userRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        } else {
            user = getCurrentUser();
        }

        String estado = dto.getEstado() != null ? dto.getEstado() : "PENDIENTE";
        if (!estado.equals("PENDIENTE") && !estado.equals("APROBADO") && !estado.equals("PAGADO")) {
            estado = "PENDIENTE";
        }

        Prestamo prestamo = Prestamo.builder()
                .capitalSolicitado(dto.getCapitalSolicitado())
                .tasaInteresAnual(dto.getTasaInteresAnual())
                .plazoMeses(dto.getPlazoMeses())
                .estado(estado)
                .usuario(user)
                .build();

        Prestamo saved = prestamoRepository.save(prestamo);

        if ("APROBADO".equals(saved.getEstado())) {
            generateAmortizationSchedule(saved);
        }

        return saved;
    }

    public Page<Prestamo> findAll(int page, int size) {
        return prestamoRepository.findAll(PageRequest.of(page, size));
    }

    public Prestamo findById(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Préstamo no encontrado"));
    }

    public List<PlanPago> findPlanPagoByPrestamoId(Long prestamoId) {
        return planPagoRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamoId);
    }

    @Transactional
    public Prestamo update(Long id, PrestamoRequestDto dto) {
        Prestamo prestamo = findById(id);

        prestamo.setCapitalSolicitado(dto.getCapitalSolicitado());
        prestamo.setTasaInteresAnual(dto.getTasaInteresAnual());
        prestamo.setPlazoMeses(dto.getPlazoMeses());

        String oldEstado = prestamo.getEstado();
        if (dto.getEstado() != null) {
            prestamo.setEstado(dto.getEstado());
        }

        if (dto.getUsuarioId() != null) {
            User user = userRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            prestamo.setUsuario(user);
        }

        Prestamo saved = prestamoRepository.save(prestamo);

        if ("APROBADO".equals(saved.getEstado()) && !"APROBADO".equals(oldEstado)) {
            // Delete old plan if any existed
            List<PlanPago> oldPlan = planPagoRepository.findByPrestamoIdOrderByNumeroCuotaAsc(saved.getId());
            for (PlanPago plan : oldPlan) {
                List<Abono> abonos = abonoRepository.findByPlanPagoId(plan.getId());
                abonoRepository.deleteAll(abonos);
                planPagoRepository.delete(plan);
            }
            generateAmortizationSchedule(saved);
        }

        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Prestamo prestamo = findById(id);
        List<PlanPago> plans = planPagoRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamo.getId());
        for (PlanPago plan : plans) {
            List<Abono> abonos = abonoRepository.findByPlanPagoId(plan.getId());
            abonoRepository.deleteAll(abonos);
            planPagoRepository.delete(plan);
        }
        prestamoRepository.delete(prestamo);
    }

    @Transactional
    public Abono createAbono(AbonoRequestDto dto) {
        PlanPago planPago = planPagoRepository.findById(dto.getPlanPagoId())
                .orElseThrow(() -> new NotFoundException("Plan de pago no encontrado"));

        if ("PAGADO".equals(planPago.getEstado())) {
            throw new BadRequestException("Esta cuota ya ha sido pagada");
        }

        BigDecimal recargo = dto.getRecargoMora() != null ? dto.getRecargoMora() : BigDecimal.ZERO;

        Abono abono = Abono.builder()
                .monto(dto.getMonto())
                .fechaPago(LocalDateTime.now())
                .recargoMora(recargo)
                .planPago(planPago)
                .build();

        Abono savedAbono = abonoRepository.save(abono);

        // actualiza plan pago state
        planPago.setEstado("PAGADO");
        planPagoRepository.save(planPago);

        // revison de todos los prestamos
        Prestamo prestamo = planPago.getPrestamo();
        List<PlanPago> allPlans = planPagoRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamo.getId());
        boolean allPaid = allPlans.stream().allMatch(p -> "PAGADO".equals(p.getEstado()));
        if (allPaid) {
            prestamo.setEstado("PAGADO");
            prestamoRepository.save(prestamo);
        }

        return savedAbono;
    }

    private void generateAmortizationSchedule(Prestamo prestamo) {
        BigDecimal capital = prestamo.getCapitalSolicitado();
        BigDecimal annualRate = prestamo.getTasaInteresAnual();
        int months = prestamo.getPlazoMeses();

        // Capital e interés divididos equitativamente (Amortización simple)
        BigDecimal capitalPorMes = capital.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        BigDecimal interesPorMes = capital.multiply(annualRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

        LocalDate date = LocalDate.now();

        for (int k = 1; k <= months; k++) {
            date = date.plusMonths(1);

            PlanPago cuota = PlanPago.builder()
                    .numeroCuota(k)
                    .montoCapital(capitalPorMes)
                    .montoInteres(interesPorMes)
                    .fechaVencimiento(date)
                    .estado("PENDIENTE")
                    .prestamo(prestamo)
                    .build();

            planPagoRepository.save(cuota);
        }
    }
}
