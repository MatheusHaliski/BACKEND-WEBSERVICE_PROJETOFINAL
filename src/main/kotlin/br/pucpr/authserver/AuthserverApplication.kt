package br.pucpr.authserver

import br.pucpr.authserver.users.AdminConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(AdminConfig::class)
class AuthserverApplication

fun main(args: Array<String>) {
	runApplication<AuthserverApplication>(*args)
}
