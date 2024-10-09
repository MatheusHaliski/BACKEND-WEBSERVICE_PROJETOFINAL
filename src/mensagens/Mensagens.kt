package br.pucpr.authserver.mensagens


import jakarta.persistence.*

@Entity
@Table(name = "mensagens")
data class Mensagens(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // ID gerado automaticamente
    val emailUser: String,
    val messageContent: String,
    val emailDestinatario: String // Destinatário
)
