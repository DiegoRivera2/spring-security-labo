package com.server.app.components;

import com.server.app.entities.Permission;
import com.server.app.entities.Role;
import com.server.app.entities.User;
import com.server.app.entities.finanzas.Categoria;
import com.server.app.entities.finanzas.TipoCategoria;
import com.server.app.repositories.PermissionRepository;
import com.server.app.repositories.RoleRepository;
import com.server.app.repositories.UserRepository;
import com.server.app.repositories.finanzas.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Order(2)
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Role adminRole = ensureRole("ADMIN");
        Role userRole = ensureRole("USER");
        assignRolePermissions(adminRole, permissionRepository.findAll());
        assignUserGetPermissions(userRole, permissionRepository.findAll());
        ensureAdminUser(adminRole);
        ensureCategorias();
    }

    private void ensureCategorias() {
        if (categoriaRepository.count() > 0) return;

        categoriaRepository.save(Categoria.builder().nombre("Salario").tipo(TipoCategoria.INGRESO).build());
        categoriaRepository.save(Categoria.builder().nombre("Transferencia recibida").tipo(TipoCategoria.INGRESO).build());
        categoriaRepository.save(Categoria.builder().nombre("Compras").tipo(TipoCategoria.EGRESO).build());
        categoriaRepository.save(Categoria.builder().nombre("Transferencia enviada").tipo(TipoCategoria.EGRESO).build());
    }

    private Role ensureRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setPermissions(new HashSet<>());
            return roleRepository.save(role);
        });
    }

    private void assignRolePermissions(Role role, List<Permission> permissions) {
        Set<Permission> current = role.getPermissions() != null ? role.getPermissions() : new HashSet<>();
        current.addAll(permissions);
        role.setPermissions(current);
        roleRepository.save(role);
    }

    private void assignUserGetPermissions(Role role, List<Permission> permissions) {
        Set<Permission> getPermissions = permissions.stream()
                .filter(p -> "GET".equalsIgnoreCase(p.getMethod()))
                .collect(java.util.stream.Collectors.toSet());
        Set<Permission> current = role.getPermissions() != null ? role.getPermissions() : new HashSet<>();
        current.addAll(getPermissions);
        role.setPermissions(current);
        roleRepository.save(role);
    }

    private void ensureAdminUser(Role adminRole) {
        if (userRepository.findUserByUsername("admin").isPresent()) {
            return;
        }

        User admin = User.builder()
                .username("admin")
                .name("Administrador")
                .surname("Sistema")
                .email("admin@uca.edu")
                .password("mi-carnet")
                .blocked(false)
                .role(adminRole)
                .build();

        userRepository.save(admin);
    }
}
