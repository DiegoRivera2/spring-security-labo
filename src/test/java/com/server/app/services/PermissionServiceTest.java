package com.server.app.services;

import com.server.app.dto.permission.PermissionDto;
import com.server.app.entities.Permission;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void createIfNotExists_debeCrearPermisoCuandoNoExiste() {
        when(permissionRepository.findByPathAndMethod("/api/nuevo", "GET")).thenReturn(Optional.empty());

        permissionService.createIfNotExists("/api/nuevo", "GET");

        verify(permissionRepository).findByPathAndMethod("/api/nuevo", "GET");
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    void createIfNotExists_noDebeGuardarCuandoYaExiste() {
        Permission existing = Permission.builder().id(1L).path("/api/nuevo").method("GET").build();
        when(permissionRepository.findByPathAndMethod("/api/nuevo", "GET")).thenReturn(Optional.of(existing));

        permissionService.createIfNotExists("/api/nuevo", "GET");

        verify(permissionRepository).findByPathAndMethod("/api/nuevo", "GET");
        verify(permissionRepository, never()).save(any());
    }

    @Test
    void update_debeActualizarTituloDelPermiso() {
        Permission permission = Permission.builder().id(5L).path("/api/users").method("GET").build();
        PermissionDto dto = new PermissionDto();
        dto.setTitle("Listar usuarios");

        when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));
        when(permissionRepository.save(permission)).thenReturn(permission);

        Permission result = permissionService.update(5L, dto);

        assertEquals("Listar usuarios", result.getTitle());
        verify(permissionRepository).findById(5L);
        verify(permissionRepository).save(permission);
    }

    @Test
    void findById_debeLanzarNotFoundCuandoNoExiste() {
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> permissionService.findById(999L));
        verify(permissionRepository).findById(999L);
    }
}
