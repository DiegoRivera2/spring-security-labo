package com.server.app.services;

import com.server.app.dto.finanzas.CuentaCreateDto;
import com.server.app.dto.finanzas.TransferenciaDto;
import com.server.app.entities.User;
import com.server.app.entities.finanzas.Categoria;
import com.server.app.entities.finanzas.Cuenta;
import com.server.app.entities.finanzas.TipoCategoria;
import com.server.app.entities.finanzas.TipoCuenta;
import com.server.app.exceptions.BadRequestException;
import com.server.app.repositories.finanzas.CategoriaRepository;
import com.server.app.repositories.finanzas.CuentaRepository;
import com.server.app.repositories.finanzas.MovimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanzaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private FinanzaService finanzaService;

    private final User usuario = User.builder().id(1).username("admin").build();

    @Test
    void crearCuenta_debePersistirCuentaConSaldoCero() {
        CuentaCreateDto dto = new CuentaCreateDto();
        dto.setAlias("Principal");
        dto.setMoneda("USD");
        dto.setTipo(TipoCuenta.AHORRO);

        Cuenta saved = Cuenta.builder().id(1L).alias("Principal").saldoBase(BigDecimal.ZERO).build();
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(saved);

        Cuenta result = finanzaService.crearCuenta(usuario, dto);

        assertEquals("Principal", result.getAlias());
        verify(cuentaRepository).save(any(Cuenta.class));
    }

    @Test
    void transferir_debeLanzarBadRequestCuandoOrigenYDestinoSonIguales() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(1L);
        dto.setMonto(BigDecimal.TEN);

        assertThrows(BadRequestException.class, () -> finanzaService.transferir(usuario, dto));
        verifyNoInteractions(cuentaRepository, movimientoRepository);
    }

    @Test
    void transferir_debeLanzarBadRequestCuandoNoHayFondosSuficientes() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setMonto(new BigDecimal("500"));

        Cuenta origen = Cuenta.builder().id(1L).saldoBase(new BigDecimal("100")).moneda("USD").build();
        Cuenta destino = Cuenta.builder().id(2L).saldoBase(BigDecimal.ZERO).moneda("USD").build();

        when(cuentaRepository.findByIdAndUsuarioId(1L, 1)).thenReturn(Optional.of(origen));
        when(cuentaRepository.findByIdAndUsuarioId(2L, 1)).thenReturn(Optional.of(destino));

        assertThrows(BadRequestException.class, () -> finanzaService.transferir(usuario, dto));
        verify(cuentaRepository, never()).save(any());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void transferir_debeActualizarSaldosYRegistrarMovimientos() {
        TransferenciaDto dto = new TransferenciaDto();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setMonto(new BigDecimal("50"));
        dto.setDescripcion("Pago");

        Cuenta origen = Cuenta.builder().id(1L).saldoBase(new BigDecimal("200")).moneda("USD").build();
        Cuenta destino = Cuenta.builder().id(2L).saldoBase(new BigDecimal("100")).moneda("USD").build();

        Categoria egreso = Categoria.builder().id(1L).nombre("Egreso").tipo(TipoCategoria.EGRESO).build();
        Categoria ingreso = Categoria.builder().id(2L).nombre("Ingreso").tipo(TipoCategoria.INGRESO).build();

        when(cuentaRepository.findByIdAndUsuarioId(1L, 1)).thenReturn(Optional.of(origen));
        when(cuentaRepository.findByIdAndUsuarioId(2L, 1)).thenReturn(Optional.of(destino));
        when(categoriaRepository.findAll()).thenReturn(List.of(egreso, ingreso));

        var movimientos = finanzaService.transferir(usuario, dto);

        assertEquals(2, movimientos.size());
        assertEquals(new BigDecimal("150"), origen.getSaldoBase());
        assertEquals(new BigDecimal("150"), destino.getSaldoBase());
        verify(cuentaRepository, times(2)).save(any(Cuenta.class));
        verify(movimientoRepository, times(2)).save(any());
    }
}
