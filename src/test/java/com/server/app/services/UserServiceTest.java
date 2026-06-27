package com.server.app.services;

import com.server.app.config.JsonWebToken;
import com.server.app.dto.auth.LoginDto;
import com.server.app.dto.auth.UpdatePasswordDto;
import com.server.app.entities.User;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.UnauthorizedException;
import com.server.app.repositories.RoleRepository;
import com.server.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JsonWebToken jsonWebToken;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void login_debeRetornarTokenCuandoCredencialesSonValidas() {
        LoginDto dto = new LoginDto();
        dto.setUsername("admin");
        dto.setPassword("mi-carnet");

        User user = User.builder()
                .id(1)
                .username("admin")
                .password("$2a$encoded")
                .blocked(false)
                .build();

        when(userRepository.findUserByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mi-carnet", "$2a$encoded")).thenReturn(true);
        when(jsonWebToken.createToken(user)).thenReturn("jwt-token");

        var response = userService.login(dto);

        assertEquals("jwt-token", response.token());
        assertEquals("admin", response.data().getUsername());
        verify(userRepository).findUserByUsername("admin");
        verify(passwordEncoder).matches("mi-carnet", "$2a$encoded");
        verify(jsonWebToken).createToken(user);
    }

    @Test
    void login_debeLanzarUnauthorizedCuandoUsuarioNoExiste() {
        LoginDto dto = new LoginDto();
        dto.setUsername("desconocido");
        dto.setPassword("pass");

        when(userRepository.findUserByUsername("desconocido")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> userService.login(dto));
        verify(userRepository).findUserByUsername("desconocido");
        verifyNoInteractions(passwordEncoder, jsonWebToken);
    }

    @Test
    void login_debeLanzarUnauthorizedCuandoCuentaEstaBloqueada() {
        LoginDto dto = new LoginDto();
        dto.setUsername("blocked");
        dto.setPassword("pass");

        User user = User.builder().username("blocked").password("hash").blocked(true).build();
        when(userRepository.findUserByUsername("blocked")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () -> userService.login(dto));
        verify(userRepository).findUserByUsername("blocked");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updatePassword_debeLanzarBadRequestCuandoPasswordActualEsIncorrecta() {
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOldPassword("wrong");
        dto.setNewPassword("NewPass1!");
        dto.setConfirmPassword("NewPass1!");

        User user = User.builder().id(1).username("admin").password("hash").blocked(false).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.updatePassword(1, dto));
        verify(userRepository).findById(1);
        verify(passwordEncoder).matches("wrong", "hash");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_debeActualizarPasswordCuandoDatosSonValidos() {
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setOldPassword("old");
        dto.setNewPassword("NewPass1!");
        dto.setConfirmPassword("NewPass1!");

        User user = User.builder().id(1).username("admin").password("hash").blocked(false).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hash")).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updatePassword(1, dto);

        assertEquals("NewPass1!", result.getPassword());
        verify(userRepository).save(user);
    }
}
