package br.pucpr.authserver.users

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("security.admin")
data class AdminConfig @ConstructorBinding constructor(
    @Value("\${security.admin.name}") val name: String,
    @Value("\${security.admin.password}") val password: String,
    @Value("\${security.admin.email}") val email: String
)
