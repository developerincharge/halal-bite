package com.halalbite.userservice.mapper;

import com.halalbite.userservice.dto.UserDto;
import com.halalbite.userservice.entity.User;
import com.halalbite.userservice.entity.UserAddress;
import org.mapstruct.*;

/**
 * UserMapper — converts between User entity and DTOs
 *
 * What is MapStruct?
 * It auto-generates the boring mapping code at COMPILE TIME.
 * Instead of writing:
 *   response.setFirstName(user.getFirstName());
 *   response.setLastName(user.getLastName());
 *   ... (10 more lines)
 *
 * You just declare the method and MapStruct writes it for you.
 * Generated code lives in target/generated-sources — you can read it!
 *
 * @Mapper(componentModel = "spring") — makes this a Spring bean
 * so you can @Autowired it anywhere.
 *
 * Why not use BeanUtils.copyProperties()?
 * That uses reflection at runtime, is slow, and silently skips
 * mismatched fields. MapStruct is compile-time, fast, and
 * gives you errors if fields are missing.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // Convert User entity → UserResponse DTO
    @Mapping(target = "addresses", source = "addresses")
    UserDto.UserResponse toResponse(User user);

    // Convert UserAddress entity → AddressResponse DTO
    UserDto.AddressResponse toAddressResponse(UserAddress address);

    // Convert CreateUserRequest → User entity
    // Ignore fields that are set by the system, not the client
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDto.CreateUserRequest request);

    // Apply partial updates from UpdateUserRequest to an existing User
    // NullValuePropertyMappingStrategy.IGNORE means null fields are skipped
    // so PATCH requests only update the fields that were actually sent
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UserDto.UpdateUserRequest request, @MappingTarget User user);
}
