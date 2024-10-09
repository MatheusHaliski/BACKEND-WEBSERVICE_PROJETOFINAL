package br.pucpr.authserver.users


import br.pucpr.authserver.roles.RoleRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
class UserController(
    val service: UserService,  // Injeção de UserService
    val roleRepository:RoleRepository
) {

    @PostMapping
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

    @DeleteMapping("/{adminId}/{userId}")
    fun delete(
        @PathVariable adminId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<Void> =
        service.delete(adminId, userId)
            ?.let { ResponseEntity.ok().build() }
            ?: ResponseEntity.notFound().build()

    @PatchMapping("/{id}")
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
        @RequestBody loginRequest: LoginRequest // Crie uma classe br.pucpr.authserver.users.LoginRequest para e-mail e senha
    ): ResponseEntity<Any> {
        return try {
            val user = service.findByEmailAndPassword(loginRequest.email, loginRequest.password)
            if (user != null) {
                ResponseEntity.ok(UserResponse(user)) // Usuário encontrado e login bem-sucedido
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Invalid email or password"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("message" to e.message))
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
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "User not found"))
        }
    }

    @PutMapping("/updateProfile")
    fun updateProfile(
        @RequestParam("email",required=false) email: String,
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



    @GetMapping("/all")
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = service.getAllUsers()
        return ResponseEntity.ok(users)
    }

}