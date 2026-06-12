package com.server.app.entities.finanzas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.server.app.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Table(name = "cuentas")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false, length = 10)
    private String moneda;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal saldoBase = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCuenta tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User usuario;
}
