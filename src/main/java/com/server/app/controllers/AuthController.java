package com.server.app.controllers;

import com.server.app.config.JsonWebToken;
import com.server.app.dto.user.*;
import com.server.app.entities.Role;
import com.server.app.entities.User;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.ConfictException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.exceptions.UnauthorizedException;
import com.server.app.dto.response.UserResponseDto;
import com.server.app.mappers.UserMapper;
import com.server.app.repositories.RoleRepository;
import com.server.app.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JsonWebToken jwtUtil;

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JsonWebToken jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        User user = userRepository.findUserByUsername(dto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (user.isBlocked()) {
            throw new UnauthorizedException("Tu cuenta ha sido bloqueada");
        }

        if (user.getRole() == null || user.getRole().getActive() == null || !user.getRole().getActive()) {
            throw new UnauthorizedException("El rol del usuario está inactivo");
        }

        String token = jwtUtil.createToken(user);
        return ResponseEntity.ok(new AuthResponseDto(token, UserMapper.toUserResponseDto(user)));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDto> signup(@Valid @RequestBody SignupRequestDto dto) {
        if (userRepository.findUserByUsername(dto.getUsername()).isPresent()) {
            throw new ConfictException("El nombre de usuario ya está en uso");
        }

        if (userRepository.findUserByEmail(dto.getEmail()).isPresent()) {
            throw new ConfictException("El correo electrónico ya está en uso");
        }

        Role role = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new NotFoundException("Rol ADMIN no encontrado"));

        if (role.getActive() == null || !role.getActive()) {
            throw new UnauthorizedException("El rol ADMIN está inactivo");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setBlocked(false);

        User savedUser = userRepository.save(user);
        String token = jwtUtil.createToken(savedUser);

        return ResponseEntity.ok(new AuthResponseDto(token, UserMapper.toUserResponseDto(savedUser)));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getProfile() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return ResponseEntity.ok(UserMapper.toUserResponseDto(user));
    }

    @PutMapping("/update/profile")
    public ResponseEntity<AuthResponseDto> updateProfile(@Valid @RequestBody UpdateProfileRequestDto dto) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!user.getUsername().equalsIgnoreCase(dto.getUsername())) {
            if (userRepository.findUserByUsername(dto.getUsername()).isPresent()) {
                throw new ConfictException("El nombre de usuario ya está en uso");
            }
        }

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (userRepository.findUserByEmail(dto.getEmail()).isPresent()) {
                throw new ConfictException("El correo electrónico ya está en uso");
            }
        }

        user.setUsername(dto.getUsername());
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());

        User updatedUser = userRepository.save(user);
        String token = jwtUtil.createToken(updatedUser);

        return ResponseEntity.ok(new AuthResponseDto(token, UserMapper.toUserResponseDto(updatedUser)));
    }

    @PutMapping("/update/password")
    public ResponseEntity<UserResponseDto> updatePassword(@Valid @RequestBody UpdatePasswordRequestDto dto) {
        User currentUser = (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User)
                ? (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        if (currentUser == null) {
            throw new UnauthorizedException("No autorizado");
        }
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getOldpassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        if (!dto.getNewpassword().equals(dto.getConfirmpassword())) {
            throw new BadRequestException("Las contraseñas nuevas no coinciden");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewpassword()));
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(UserMapper.toUserResponseDto(updatedUser));
    }
}
