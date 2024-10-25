package br.pucpr.authserver.users

import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.security.Jwt
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
class UserController(
    val service: UserService,  // Injeção de UserService
    val roleRepository: RoleRepository,
    private val jwt: Jwt
) {

    @PostMapping
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun insert(@RequestBody @Valid userRequest: CreateUserRequest): ResponseEntity<Any> {
        return try {
            // Busca o Role baseado no roleId fornecido no request
            val role = roleRepository.findByName(userRequest.role)
                ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("message" to "Role com ID ${userRequest.roleId} não encontrado."))

            // Converte o CreateUserRequest para User, passando o Role
            val newUser = service.insert(userRequest)

            ResponseEntity.status(HttpStatus.CREATED).body(UserResponse(newUser))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to e.message))
        }
    }

    @GetMapping
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun list(
        @RequestParam(required = false) sortDir: String?
    ) = SortDir.getByName(sortDir)
        ?.let { service.list(it) }
        ?.map { UserResponse(it) }
        ?.let { ResponseEntity.ok(it) }
        ?: ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

    @GetMapping("/{id}")
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun findById(@PathVariable id: Long) =
        service.findByIdOrNull(id)
            ?.let { UserResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun update(@PathVariable id: Long, @RequestBody @Valid userRequest: CreateUserRequest): ResponseEntity<out Any> {
        // Busca o Role baseado no roleId fornecido no request
        val role = service.findRoleById(userRequest.role)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Role com ID ${userRequest.roleId} não encontrado."))

        // Converte o CreateUserRequest para User, passando o Role
        val updatedUser = service.update(id, userRequest.toUser(role))

        return if (updatedUser != null) {
            ResponseEntity.ok(UserResponse(updatedUser)) // Usa o construtor que aceita User
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{userId}/{roleId}")
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun addRoleToUser(
        @PathVariable userId: Long,
        @PathVariable roleId: Long
    ): ResponseEntity<UserResponse> =
        service.addRoleToUser(userId, roleId)
            ?.let { UserResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping("/login")
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        return try {
            val user = service.findByEmailAndPassword(loginRequest.email, loginRequest.password)
            if (user != null) {
                // Gerar o token JWT para o usuário logado
                val token = jwt.createToken(user)

                // Retornar o token no corpo da resposta
                ResponseEntity.ok(mapOf("token" to token, "user" to UserResponse(user)))
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Invalid email or password"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("message" to e.message))
        }
    }

    @PostMapping("/reset-password")
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun resetPassword(
        @RequestBody request: ResetPasswordRequest // Classe para encapsular o e-mail e nova senha no request
    ): ResponseEntity<Any> {
        val user = service.findByEmail(request.email)
        return if (user != null) {
            val updateSuccessful = service.updatePassword(user, request.newPassword) // Método para atualizar a senha
            if (updateSuccessful) {
                ResponseEntity.ok(mapOf("message" to "Password has been successfully reset"))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("message" to "Failed to reset password"))
            }
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "Email not found"))
        }
    }

    @GetMapping("/")
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun getProfile(@RequestParam email: String): ResponseEntity<Any> {
        val user = service.findByEmail(email)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "User not found"))
        }
    }

    @PutMapping("/")
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun updateProfile(
        @RequestParam("email", required = false) email: String,
        @RequestParam("name") name: String,
        @RequestParam("profilePic", required = false) profilePic: MultipartFile?
    ): ResponseEntity<Any> {
        val updated = service.updateProfile(email, name, profilePic)
        return if (updated) {
            ResponseEntity.ok(mapOf("message" to "Profile updated successfully"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to "Failed to update profile"))
        }
    }

    @DeleteMapping("/{adminId}/{userId}")
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(
        @PathVariable adminId: String,
        @PathVariable userId: Long
    ): ResponseEntity<Any> {
        // Verifica se o adminId pertence a um usuário com a role de "ADMIN"
        val adminUser = service.findByEmail(adminId)
        if (adminUser == null || !adminUser.roles.any { it.name == "ADMIN" }) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("message" to "You do not have permission to delete users. Admin role required."))
        }

        // Obtenha o email do usuário com base no userId (presumindo que existe um método para isso)
        val userEmail = service.findEmailByUserId(userId) // Implemente este método

        // Deletar mensagens associadas ao usuário antes de deletar o usuário
        val messagesDeleted = userEmail?.let { service.deleteMessagesByUserId(it) }

        // Caso o usuário seja um admin, permite a exclusão do usuário especificado
        return service.delete(userId)?.let {
            ResponseEntity.ok().body(mapOf("message" to "User and related messages deleted successfully"))
        } ?: ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("message" to "User not found"))
    }

    @GetMapping("/all")
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = service.getAllUsers()
        return ResponseEntity.ok(users)
    }
}
