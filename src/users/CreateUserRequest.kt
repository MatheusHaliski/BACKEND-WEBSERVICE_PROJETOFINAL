package br.pucpr.authserver.users

import br.pucpr.authserver.roles.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import javax.annotation.processing.Generated

data class CreateUserRequest(
    @field:NotBlank
    val name: String?,

    @field:NotNull
    @field:Email
    val email: String?,

    @field:NotNull
    @field:Size(min=4)
    val password: String?,

    @field:NotNull
    @Generated
    val roleId: Long,

    val role: String
)
{
    fun toUser(role: Role) = User(
        name = name!!,
        email = email!!,
        password = password!!,
        roles = mutableListOf(role) // Associa o role ao usuário
    )
}

