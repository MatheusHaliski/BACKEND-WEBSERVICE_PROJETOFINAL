package br.pucpr.authserver.security

import br.pucpr.authserver.users.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.jackson.io.JacksonDeserializer
import io.jsonwebtoken.jackson.io.JacksonSerializer
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date

@Component
class Jwt {

    fun createToken(user: User): String {
        val userToken = UserToken(user)
        return Jwts.builder()
            .serializeToJsonWith(JacksonSerializer())
            .signWith(Keys.hmacShaKeyFor(SECRET.toByteArray()))
            .setIssuedAt(utcNow().toDate())
            .setExpiration(
                utcNow().plusHours(
                    if (userToken.isAdmin) ADMIN_EXPIRE_HOURS else EXPIRE_HOURS
                ).toDate()
            )
            .setIssuer(ISSUER)
            .setSubject(userToken.id.toString())
            .claim(USER_FIELD, userToken)
            .compact()
    }

    fun extract(req: HttpServletRequest): Authentication? {
        try {
            val header = req.getHeader(AUTHORIZATION) ?: return null
            if (!header.startsWith("Bearer ")) return null

            val token = header.removePrefix("Bearer ")
            val claims = Jwts.parserBuilder()
                .deserializeJsonWith(
                    JacksonDeserializer(mapOf(USER_FIELD to UserToken::class.java))
                )
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.toByteArray()))
                .build()
                .parseClaimsJws(token)
                .body

            // Aplicar regras de segurança personalizadas
            if (claims.issuer != ISSUER) {
                log.debug("Token rejeitado: $ISSUER != ${claims.issuer}")
                return null
            }

            return claims.get(USER_FIELD, UserToken::class.java).toAuthentication()
        } catch (e: Throwable) {
            log.debug("Token rejeitado", e)
            return null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Jwt::class.java)
        private const val SECRET = "0e5582adfb7fa6bb770815f3c6b3534d311bd5fe" // Use uma chave mais segura em produção
        private const val EXPIRE_HOURS = 48L
        private const val ADMIN_EXPIRE_HOURS = 2L
        private const val ISSUER = "PUCPR AuthServer"
        private const val USER_FIELD = "User"

        private fun utcNow() = ZonedDateTime.now(ZoneOffset.UTC)
        private fun ZonedDateTime.toDate() = Date.from(this.toInstant())

        private fun UserToken.toAuthentication(): Authentication {
            val authorities = this.roles.map { SimpleGrantedAuthority("ROLE_$it") }
            return UsernamePasswordAuthenticationToken(this, id, authorities)
        }
    }
}
