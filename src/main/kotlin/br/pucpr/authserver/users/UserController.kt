package br.pucpr.authserver.users

import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.users.requests.*
import br.pucpr.authserver.users.responses.UserResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
class UserController(
    val service: UserService,  // Injeção de UserService
    val roleRepository: RoleRepository
) {

    @PostMapping
    fun insert(@RequestBody @Valid userRequest: CreateUserRequest): ResponseEntity<Any> {
        return try {
            // Busca o Role baseado no role fornecido no request
            val role = roleRepository.findByName(userRequest.role)
                ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("message" to "Role com nome ${userRequest.role} não encontrado."))

            // Converte o CreateUserRequest para User, passando o Role
            val newUser = service.insert(userRequest)

            ResponseEntity.status(HttpStatus.CREATED).body(UserResponse(newUser))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to e.message))
        }
    }

    @GetMapping("/list")
    fun listUsers(): ResponseEntity<List<UserResponse>> {
        val users = service.getAllUsers()
        return if (users.isNotEmpty()) {
            ResponseEntity.ok(users.map { UserResponse(it) }) // Map users to UserResponse DTO
        } else {
            ResponseEntity.noContent().build() // No users found
        }
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) sortDir: String?
    ) = SortDir.getByName(sortDir)
        ?.let { service.list(it) }
        ?.map { UserResponse(it) }
        ?.let { ResponseEntity.ok(it) }
        ?: ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) =
        service.findByIdOrNull(id)
            ?.let { UserResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid userRequest: CreateUserRequest): ResponseEntity<out Any> {
        // Busca o Role baseado no nome fornecido no request
        val role = roleRepository.findByName(userRequest.role)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Role com nome ${userRequest.role} não encontrado."))

        // Converte o CreateUserRequest para User, passando o Role
        val updatedUser = service.update(id, userRequest.toUser(role))

        return if (updatedUser != null) {
            ResponseEntity.ok(UserResponse(updatedUser)) // Usa o construtor que aceita User
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{userId}/roles/{roleId}")
    fun addRoleToUser(
        @PathVariable userId: Long,
        @PathVariable roleId: Long
    ): ResponseEntity<UserResponse> =
        service.addRoleToUser(userId, roleId)
            ?.let { UserResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest // Classe br.pucpr.authserver.users.requests.LoginRequest para e-mail e senha
    ): ResponseEntity<Any> {
        return try {
            val token = service.login(loginRequest.email, loginRequest.password)
            ResponseEntity.ok(mapOf("token" to token)) // Retorna o token JWT na resposta
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Invalid email or password"))
        }
    }

    @PostMapping("/reset-password")
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

    @GetMapping("/profile")
    fun getProfile(@RequestParam email: String): ResponseEntity<Any> {
        val user = service.findByEmail(email)
        return if (user != null) {
            ResponseEntity.ok(UserResponse(user))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "User not found"))
        }
    }

    @PutMapping("/updateProfile")
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
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = service.getAllUsers()
        return ResponseEntity.ok(users.map { UserResponse(it) })
    }

}
