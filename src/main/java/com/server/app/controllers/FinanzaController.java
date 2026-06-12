package com.server.app.controllers;

import com.server.app.dto.finanzas.CuentaCreateDto;
import com.server.app.dto.finanzas.TransferenciaDto;
import com.server.app.dto.response.Pagination;
import com.server.app.dto.response.PaginationMeta;
import com.server.app.entities.User;
import com.server.app.entities.finanzas.Categoria;
import com.server.app.entities.finanzas.Cuenta;
import com.server.app.entities.finanzas.Movimiento;
import com.server.app.services.FinanzaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/finanzas")
public class FinanzaController {

    private final FinanzaService finanzaService;

    public FinanzaController(FinanzaService finanzaService) {
        this.finanzaService = finanzaService;
    }

    @GetMapping("/cuentas")
    public ResponseEntity<Pagination<Cuenta>> listarCuentas(@AuthenticationPrincipal User user) {
        List<Cuenta> cuentas = finanzaService.listarCuentas(user);
        return ResponseEntity.ok(new Pagination<>(
                cuentas,
                new PaginationMeta(0, cuentas.size(), 1, cuentas.size())
        ));
    }

    @PostMapping("/cuentas")
    public ResponseEntity<Cuenta> crearCuenta(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody CuentaCreateDto dto) {
        return ResponseEntity.ok(finanzaService.crearCuenta(user, dto));
    }

    @GetMapping("/movimientos")
    public ResponseEntity<Pagination<Movimiento>> listarMovimientos(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        Page<Movimiento> p = finanzaService.listarMovimientos(user, page, size, desde, hasta);
        return ResponseEntity.ok(new Pagination<>(
                p.getContent(),
                new PaginationMeta(p.getNumber(), p.getSize(), p.getTotalPages(), p.getTotalElements())
        ));
    }

    @PostMapping("/transferencias")
    public ResponseEntity<List<Movimiento>> transferir(@AuthenticationPrincipal User user,
                                                       @Valid @RequestBody TransferenciaDto dto) {
        return ResponseEntity.ok(finanzaService.transferir(user, dto));
    }

    @GetMapping("/categorias")
    public ResponseEntity<Pagination<Categoria>> listarCategorias() {
        List<Categoria> categorias = finanzaService.listarCategorias();
        return ResponseEntity.ok(new Pagination<>(
                categorias,
                new PaginationMeta(0, categorias.size(), 1, categorias.size())
        ));
    }
}
