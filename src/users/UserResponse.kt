package br.pucpr.authserver.users

data class UserResponse(
    val id: Long,
    val name: String?,
    val email: String?
) {
    constructor(user: User): this(
        id = user.id!!,
        name = user.name,
        email = user.email
    )
}
