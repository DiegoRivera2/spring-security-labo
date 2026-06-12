package com.server.app.repositories.finanzas;

import com.server.app.entities.finanzas.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByUsuarioId(int usuarioId);

    Optional<Cuenta> findByIdAndUsuarioId(Long id, int usuarioId);
}
