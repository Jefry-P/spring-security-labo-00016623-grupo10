package com.server.app.mappers;

import com.server.app.dto.response.PermissionResponseDto;
import com.server.app.dto.response.RoleResponseDto;
import com.server.app.dto.response.UserResponseDto;
import com.server.app.entities.Permission;
import com.server.app.entities.Role;
import com.server.app.entities.User;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .role(toRoleResponseDto(user.getRole()))
                .build();
    }

    public static RoleResponseDto toRoleResponseDto(Role role) {
        if (role == null) {
            return null;
        }
        return RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(role.getPermissions() != null ? role.getPermissions().stream()
                        .map(UserMapper::toPermissionResponseDto)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    public static PermissionResponseDto toPermissionResponseDto(Permission permission) {
        if (permission == null) {
            return null;
        }
        return PermissionResponseDto.builder()
                .id(permission.getId())
                .path(permission.getPath())
                .method(permission.getMethod())
                .build();
    }
}
