package br.pucpr.authserver.users

import br.pucpr.authserver.users.errors.NotFoundException
import br.pucpr.authserver.roles.Role
import br.pucpr.authserver.roles.RoleRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Service
class UserService(
    private val repository: UserRepository,
    private val roleRepository: RoleRepository
) {
    fun isUserNameTaken(name: String): Boolean {
        return repository.findByName(name) != null
    }
    fun isUserEmailTaken(name: String): Boolean {
        return repository.findByEmail(name) != null
    }
    fun findByEmailAndPassword(email: String, password: String): User? {
        return repository.findByEmailAndPassword(email, password) // Corrigido para retornar um User
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
    fun findRoleById(roleId: String): Role? {
        return roleRepository.findByName(roleId).orElse(null)
    }

    fun list(sortDir: SortDir): List<User> =
        if (sortDir == SortDir.ASC)
            repository.findAll()
        else
            repository.findAll().reversed()

    fun findByIdOrNull(id: Long): User? = repository.findById(id).orElse(null)

    fun update(id: Long, user: User): User {
        val existingUser: User = repository.findById(id).orElse(null)
            ?: throw NotFoundException("Usuário com ID $id não encontrado!")

        existingUser.name = user.name ?: existingUser.name
        existingUser.email = user.email ?: existingUser.email
        existingUser.password = user.password ?: existingUser.password
        existingUser.roles = user.roles ?: existingUser.roles
        return repository.save(existingUser)
    }

    fun updateEmail(userId: Long, newEmail: String): User {
        val user: User = repository.findById(userId).orElseThrow {
            NotFoundException("Usuário com ID $userId não encontrado!")
        }

        if (repository.findByEmail(newEmail) != null) {
            throw IllegalArgumentException("O e-mail '$newEmail' já está em uso.")
        }

        user.email = newEmail
        return repository.save(user)
    }

    fun delete(adminId: Long, userId: Long) {
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

    fun addRoleToUser(userId: Long, roleId: Long): User? {
        val user: User = repository.findById(userId).orElseThrow {
            NotFoundException("Usuário com ID $userId não encontrado!")
        }

        val role: Role = roleRepository.findById(roleId).orElseThrow {
            NotFoundException("Role com ID $roleId não encontrado!")
        }

        if (!user.roles.contains(role)) {
            user.roles.add(role)
            return repository.save(user)
        } else {
            throw IllegalArgumentException("Usuário já possui este Role!")
        }
    }
    fun findByEmail(email: String): User? {
        return repository.findByEmail(email)
    }
    fun updatePassword(user: User, newPassword: String): Boolean {
        user.password = newPassword
        repository.save(user)
        return true
    }
    @Transactional
    fun updateProfile(email: String, name: String?, profilePic: MultipartFile?): Boolean {
        val user = repository.findByEmail(email) ?: return false

        user.name = name ?: user.name

        if (profilePic != null) {
            val fileName = "${profilePic.originalFilename}"
            val filePath = Paths.get("src/main/resources/static/uploads", fileName)
            profilePic.inputStream.use { input ->
                Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING)
            }
            user.profilePic = fileName // Save only the file name or URL
        }

        repository.save(user)
        return true
    }
    fun getAllUsers(): List<User> {
        val users = repository.findAll()
        println("Fetched users: $users")
        return users
    }
}

