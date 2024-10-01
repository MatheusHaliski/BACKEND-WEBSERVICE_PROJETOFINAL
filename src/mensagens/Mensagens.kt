package br.pucpr.authserver.mensagens


import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Mensagens(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // ID gerado automaticamente
    val emailUser: String,
    val messageContent: String,
    val emailDestinatario: String // Destinatário
)
