package br.pucpr.authserver.users

// Classe para encapsular os dados da solicitação de redefinição de senha
data class ResetPasswordRequest(
    val email: String,
    val newPassword: String
)
