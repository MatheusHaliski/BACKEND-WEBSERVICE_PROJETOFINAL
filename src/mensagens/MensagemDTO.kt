package br.pucpr.authserver.mensagens

data class MensagemDTO(
    val emailUser: String,
    val emailDestinatario: String,
    val messageContent: String
)
