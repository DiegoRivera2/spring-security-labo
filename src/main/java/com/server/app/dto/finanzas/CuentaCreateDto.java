package com.server.app.dto.finanzas;

import com.server.app.entities.finanzas.TipoCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CuentaCreateDto {

    @NotBlank(message = "El alias es obligatorio")
    private String alias;

    @NotBlank(message = "La moneda es obligatoria")
    private String moneda;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private TipoCuenta tipo;
}
