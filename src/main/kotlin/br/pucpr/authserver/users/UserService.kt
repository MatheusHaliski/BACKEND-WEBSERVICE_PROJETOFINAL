package br.pucpr.authserver.users

import br.pucpr.authserver.mensagens.MensagensGRepository
import br.pucpr.authserver.users.errors.NotFoundException
import br.pucpr.authserver.roles.Role
import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.security.Jwt // Certifique-se de ter a classe Jwt importada
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Service
class UserService(
    private val repository: UserRepository,
    private val roleRepository: RoleRepository,
    private val mensagensGRepository:MensagensGRepository,
    private val jwt: Jwt // Adicione o JWT ao construtor
) {
    // Verifica o token antes de executar a lógica
    private fun verifyToken(token: String) {
        if (!jwt.validateToken(token)) {
            throw IllegalArgumentException("Token inválido ou expirado!")
        }
    }

    fun login(email: String, password: String): LoginResponse? {
        val user = repository.findByEmailAndPassword(email, password)
            ?: throw NotFoundException("Usuário ou senha incorretos!")

        // Gere o token JWT
        val token = jwt.createToken(user)

        return LoginResponse(
            token = token,
            user = UserResponse(user)
        )
    }

    fun insert(userRequest: CreateUserRequest): User {
        val role = roleRepository.findByName(userRequest.role)
            .orElseThrow { IllegalArgumentException("Role com ID ${userRequest.roleId} não encontrado.") }

        val user = userRequest.toUser(role)

        if (user.name?.let { isUserNameTaken(it) } == true) {
            throw IllegalArgumentException("O nome '${user.name}' já está em uso.")
        }
        if (user.email?.let { isUserEmailTaken(it) } == true) {
            throw IllegalArgumentException("O email '${user.email}' já está em uso.")
        }

        return repository.save(user)
    }

    // Exemplo de verificação de token para listar usuários
    fun list(sortDir: SortDir, token: String): List<User> {
        verifyToken(token)
        return if (sortDir == SortDir.ASC)
            repository.findAll()
        else
            repository.findAll().reversed()
    }

    fun findByIdOrNull(id: Long, token: String): User? {
        verifyToken(token)
        return repository.findById(id).orElse(null)
    }

    fun update(id: Long, user: User, token: String): User {
        verifyToken(token)
        val existingUser: User = repository.findById(id).orElse(null)
            ?: throw NotFoundException("Usuário com ID $id não encontrado!")

        existingUser.name = user.name ?: existingUser.name
        existingUser.email = user.email ?: existingUser.email
        existingUser.password = user.password ?: existingUser.password
        existingUser.roles = user.roles ?: existingUser.roles
        return repository.save(existingUser)
    }

    fun delete(adminId: Long, userId: Long, token: String) {
        verifyToken(token)

        val admin: User = repository.findById(adminId).orElseThrow {
            NotFoundException("Administrador com ID $adminId não encontrado!")
        }

        if (!admin.roles.any { it.name == "ADM" }) {
            throw NotFoundException("Apenas administradores podem excluir usuários!")
        }

        val user: User = repository.findById(userId).orElseThrow {
            NotFoundException("Usuário com ID $userId não encontrado!")
        }

        repository.deleteById(userId)
    }



}
