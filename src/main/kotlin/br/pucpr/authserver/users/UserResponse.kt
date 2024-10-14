package br.pucpr.authserver.users

import br.pucpr.authserver.roles.Role

data class UserResponse(
    val id: Long,
    val name: String?,
    val email: String?,
    val roles: MutableList<Role>
) {
    constructor(user: User): this(
        id = user.id!!,
        name = user.name,
        email = user.email,
        roles = user.roles
    )
}
