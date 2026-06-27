package com.server.app.services;

import com.server.app.dto.permission.AssingPermissionDto;
import com.server.app.dto.role.RoleDto;
import com.server.app.entities.Permission;
import com.server.app.entities.Role;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.PermissionRepository;
import com.server.app.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void save_debeCrearRolConPermisosAsignados() {
        RoleDto dto = new RoleDto();
        dto.setName("EDITOR");

        AssingPermissionDto ap1 = new AssingPermissionDto();
        ap1.setId(1L);
        AssingPermissionDto ap2 = new AssingPermissionDto();
        ap2.setId(2L);
        dto.setPermissions(Set.of(ap1, ap2));

        Permission p1 = Permission.builder().id(1L).path("/api/test").method("GET").build();
        Permission p2 = Permission.builder().id(2L).path("/api/test").method("POST").build();

        when(permissionRepository.findAllById(any())).thenReturn(List.of(p1, p2));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = roleService.save(dto);

        assertEquals("EDITOR", result.getName());
        assertEquals(2, result.getPermissions().size());
        verify(permissionRepository).findAllById(any());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void update_debeLanzarNotFoundCuandoRolNoExiste() {
        RoleDto dto = new RoleDto();
        dto.setName("USER");

        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.update(99L, dto));
        verify(roleRepository).findById(99L);
        verify(roleRepository, never()).save(any());
    }

    @Test
    void delete_debeInvocarRepositorio() {
        roleService.delete(3L);
        verify(roleRepository).deleteById(3L);
    }
}
