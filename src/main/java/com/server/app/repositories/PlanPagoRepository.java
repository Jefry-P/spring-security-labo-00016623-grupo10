package com.server.app.repositories;

import com.server.app.entities.PlanPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanPagoRepository extends JpaRepository<PlanPago, Long> {
    List<PlanPago> findByPrestamoIdOrderByNumeroCuotaAsc(Long prestamoId);
}
