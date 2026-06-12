package com.server.app.repositories.finanzas;

import com.server.app.entities.finanzas.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    Page<Movimiento> findByUsuarioIdOrderByFechaDesc(int usuarioId, Pageable pageable);

    Page<Movimiento> findByUsuarioIdAndFechaBetweenOrderByFechaDesc(
            int usuarioId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    Page<Movimiento> findByUsuarioIdAndFechaGreaterThanEqualOrderByFechaDesc(
            int usuarioId, LocalDateTime desde, Pageable pageable);

    Page<Movimiento> findByUsuarioIdAndFechaLessThanEqualOrderByFechaDesc(
            int usuarioId, LocalDateTime hasta, Pageable pageable);
}
