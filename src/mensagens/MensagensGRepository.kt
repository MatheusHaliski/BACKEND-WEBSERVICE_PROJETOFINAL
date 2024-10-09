package br.pucpr.authserver.mensagens



import br.pucpr.authserver.mensagens.MensagensG
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MensagensGRepository : JpaRepository<MensagensG, Long> {
    fun findByEmailUser(emailUser: String): List<MensagensG>
}
