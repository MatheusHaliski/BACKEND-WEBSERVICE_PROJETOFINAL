package br.pucpr.authserver.roles

data class RoleResponse(
    val id: Long?, // Adiciona o campo id
    val name: String,
    val description: String
) {
    constructor(role: Role) : this(
        id = role.id, // Assume que o objeto Role tem um campo id
        name = role.name,
        description = role.description
    )
}
