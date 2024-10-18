package br.pucpr.authserver.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByName(name: String): User?
    fun findByEmailAndPassword(email: String,senha: String): User?
    fun findByRoles_Name(roleName: String?): List<User?>?


}
