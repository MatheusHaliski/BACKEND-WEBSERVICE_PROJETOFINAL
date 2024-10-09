package br.pucpr.authserver.users

import br.pucpr.authserver.mensagens.Mensagens
import br.pucpr.authserver.roles.Role
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:NotBlank
    @field:Size(max = 100)
    var name: String? = null,

    @field:NotBlank
    @field:Email
    @field:Size(max = 100)
    var email: String? = null,

    @field:NotBlank
    @field:Size(max = 100)
    var password: String? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
    name = "users_roles",
    joinColumns = [JoinColumn(name = "users_id")],
    inverseJoinColumns = [JoinColumn(name = "roles_id")]
)
    var roles: MutableList<Role> = mutableListOf(),
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_mensagens",
        joinColumns = [JoinColumn(name = "users_id")],
        inverseJoinColumns = [JoinColumn(name = "mensagens_id")]
    )
   var mensagens: MutableSet<Mensagens> = mutableSetOf(),

    var profilePic: String? = null,



)
