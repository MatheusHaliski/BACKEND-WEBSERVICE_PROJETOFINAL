
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.handler.HandlerMappingIntrospector

@Configuration
@EnableWebMvc
@SecurityScheme(
    name="AuthServer",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)

class SecurityConfig {
    @Bean
    fun mvc(instrospector: HandlerMappingIntrospector) = MvcRequestMatcher.Builder(instrospector)

    @Bean
    fun corsFilter() =
        CorsConfiguration().apply {
            addAllowedHeader("*")
            addAllowedMethod("*")
            addAllowedOrigin("*")
        }.let{
            UrlBasedCorsConfigurationSource().apply{
                registerCorsConfiguration("/**", it)
            }
        }.let{
            CorsFilter(it)
        }


    fun filterChain(security: HTTPSecurity, mvc: MvcRequestMatcher.Builder):
            SecurityFilterChain =
        security {
            .sessionManagement { it.sessionCreationPolicy(STATELESS) }
            .cors(Customizer.withDefaults())
            .csfr { it.disable() }
            .headers { it.frameOption { fo -> fo.disable() } }
            .authorizeHttpRequests { requests ->
                requests.anyRequest().permitAll()
            }
        }.build()
}
