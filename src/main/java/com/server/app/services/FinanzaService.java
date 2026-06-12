package com.server.app.services;

import com.server.app.dto.finanzas.CuentaCreateDto;
import com.server.app.dto.finanzas.TransferenciaDto;
import com.server.app.entities.User;
import com.server.app.entities.finanzas.*;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.finanzas.CategoriaRepository;
import com.server.app.repositories.finanzas.CuentaRepository;
import com.server.app.repositories.finanzas.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanzaService {

    private final CuentaRepository cuentaRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;

    public List<Cuenta> listarCuentas(User usuario) {
        return cuentaRepository.findByUsuarioId(usuario.getId());
    }

    @Transactional
    public Cuenta crearCuenta(User usuario, CuentaCreateDto dto) {
        Cuenta cuenta = Cuenta.builder()
                .alias(dto.getAlias())
                .moneda(dto.getMoneda())
                .tipo(dto.getTipo())
                .saldoBase(BigDecimal.ZERO)
                .usuario(usuario)
                .build();
        return cuentaRepository.save(cuenta);
    }

    public Page<Movimiento> listarMovimientos(User usuario, int page, int size,
                                               LocalDateTime desde, LocalDateTime hasta) {
        int userId = usuario.getId();
        PageRequest pageable = PageRequest.of(page, size);

        if (desde != null && hasta != null) {
            return movimientoRepository.findByUsuarioIdAndFechaBetweenOrderByFechaDesc(
                    userId, desde, hasta, pageable);
        }
        if (desde != null) {
            return movimientoRepository.findByUsuarioIdAndFechaGreaterThanEqualOrderByFechaDesc(
                    userId, desde, pageable);
        }
        if (hasta != null) {
            return movimientoRepository.findByUsuarioIdAndFechaLessThanEqualOrderByFechaDesc(
                    userId, hasta, pageable);
        }
        return movimientoRepository.findByUsuarioIdOrderByFechaDesc(userId, pageable);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public List<Movimiento> transferir(User usuario, TransferenciaDto dto) {
        if (dto.getCuentaOrigenId().equals(dto.getCuentaDestinoId())) {
            throw new BadRequestException("La cuenta origen y destino deben ser diferentes");
        }

        Cuenta origen = cuentaRepository.findByIdAndUsuarioId(dto.getCuentaOrigenId(), usuario.getId())
                .orElseThrow(() -> new NotFoundException("Cuenta origen no encontrada"));

        Cuenta destino = cuentaRepository.findByIdAndUsuarioId(dto.getCuentaDestinoId(), usuario.getId())
                .orElseThrow(() -> new NotFoundException("Cuenta destino no encontrada"));

        if (origen.getSaldoBase().compareTo(dto.getMonto()) < 0) {
            throw new BadRequestException("Fondos insuficientes en la cuenta origen");
        }

        Categoria categoriaEgreso = categoriaRepository.findAll().stream()
                .filter(c -> c.getTipo() == TipoCategoria.EGRESO)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No hay categorías de egreso configuradas"));

        Categoria categoriaIngreso = categoriaRepository.findAll().stream()
                .filter(c -> c.getTipo() == TipoCategoria.INGRESO)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No hay categorías de ingreso configuradas"));

        origen.setSaldoBase(origen.getSaldoBase().subtract(dto.getMonto()));
        destino.setSaldoBase(destino.getSaldoBase().add(dto.getMonto()));
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        LocalDateTime ahora = LocalDateTime.now();
        String descripcion = dto.getDescripcion() != null ? dto.getDescripcion() : "Transferencia entre cuentas";

        Movimiento egreso = Movimiento.builder()
                .monto(dto.getMonto().negate())
                .monedaOriginal(origen.getMoneda())
                .tasaCambio(BigDecimal.ONE)
                .fecha(ahora)
                .descripcion("Egreso: " + descripcion)
                .cuenta(origen)
                .categoria(categoriaEgreso)
                .usuario(usuario)
                .build();

        Movimiento ingreso = Movimiento.builder()
                .monto(dto.getMonto())
                .monedaOriginal(destino.getMoneda())
                .tasaCambio(BigDecimal.ONE)
                .fecha(ahora)
                .descripcion("Ingreso: " + descripcion)
                .cuenta(destino)
                .categoria(categoriaIngreso)
                .usuario(usuario)
                .build();

        movimientoRepository.save(egreso);
        movimientoRepository.save(ingreso);

        return List.of(egreso, ingreso);
    }
}
