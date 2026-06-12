package com.server.app.repositories.finanzas;

import com.server.app.entities.finanzas.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
