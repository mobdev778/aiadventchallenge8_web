package com.github.mobdev778.aiadventchallenge.data.chatclient.repository

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.RoleDto
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.Role
import org.springframework.stereotype.Component

@Component
class RoleMapper {

    fun map(dto: RoleDto): Role {
        return when (dto) {
            RoleDto.System -> Role.System
            RoleDto.User -> Role.User
            RoleDto.Assistant -> Role.Assistant
            RoleDto.Developer -> Role.Developer
            RoleDto.Tool -> Role.Tool
        }
    }

    fun map(role: Role): RoleDto {
        return when (role) {
            Role.System -> RoleDto.System
            Role.User -> RoleDto.User
            Role.Assistant -> RoleDto.Assistant
            Role.Developer -> RoleDto.Developer
            Role.Tool -> RoleDto.Tool
        }
    }
}
