package br.pucpr.authserver.users

data class LoginRequest(
    val email: String,
    val password: String
)
