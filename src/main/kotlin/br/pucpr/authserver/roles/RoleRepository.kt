package br.pucpr.authserver.roles

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository: JpaRepository<Role, Long> {
    fun findByName(name: String):  Role? // Agora está correto para buscar pelo nome
}

