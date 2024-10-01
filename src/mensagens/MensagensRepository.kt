package br.pucpr.authserver.mensagens



import br.pucpr.authserver.mensagens.Mensagens
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MensagensRepository : JpaRepository<Mensagens, Long> {
    fun findByEmailUser(emailUser: String): List<Mensagens>
    fun findByEmailDestinatario(emailDestinatario: String): List<Mensagens>
    fun findByEmailUserAndEmailDestinatario(email1: String, email2: String): List<Mensagens>
}
