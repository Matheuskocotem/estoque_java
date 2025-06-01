package com.example.estoque.api.mapper.user;

import com.example.estoque.api.dto.user.UserCreateDTO;
import com.example.estoque.api.dto.user.UserResponseDTO;
import com.example.estoque.domain.model.user.Role;
import com.example.estoque.domain.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", source = "role", qualifiedByName = "stringToRole")
    User toEntity(UserCreateDTO dto);

    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    UserResponseDTO toDTO(User user);

    @Named("stringToRole")
    default Role stringToRole(String roleName) {
        if (roleName == null) {
            return null;
        }
        try {
            return Role.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    @Named("roleToString")
    default String roleToString(Role role) {
        if (role == null) {
            return null;
        }
        return role.name();
    }
}
