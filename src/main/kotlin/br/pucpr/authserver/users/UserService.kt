package br.pucpr.authserver.users
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import br.pucpr.authserver.mensagens.MensagensGRepository
import br.pucpr.authserver.users.errors.NotFoundException
import br.pucpr.authserver.roles.Role
import br.pucpr.authserver.roles.RoleRepository
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
    private val mensagensGRepository: MensagensGRepository,
    private val log: Logger = LoggerFactory.getLogger(UserService::class.java)
) {
    fun isUserNameTaken(name: String): Boolean {
        log.info("Verificando se o nome do usuário '$name' está em uso.")
        return repository.findByName(name) != null
    }

    fun isUserEmailTaken(email: String): Boolean {
        log.info("Verificando se o email '$email' está em uso.")
        return repository.findByEmail(email) != null
    }

    fun findByEmailAndPassword(email: String, password: String): User? {
        log.info("Buscando usuário com email '$email' e senha fornecida.")
        return repository.findByEmailAndPassword(email, password)
    }

    fun insert(userRequest: CreateUserRequest): User {
        log.info("Inserindo novo usuário com email '${userRequest.email}'.")
        val role = roleRepository.findByName(userRequest.role)
            ?: throw IllegalArgumentException("Role com ID ${userRequest.roleId} não encontrado.")

        val user = userRequest.toUser(role)

        if (user.name?.let { isUserNameTaken(it) } == true) {
            log.warn("O nome '${user.name}' já está em uso.")
            throw IllegalArgumentException("O nome '${user.name}' já está em uso.")
        }

        if (user.email?.let { isUserEmailTaken(it) } == true) {
            log.warn("O email '${user.email}' já está em uso.")
            throw IllegalArgumentException("O email '${user.email}' já está em uso.")
        }

        return repository.save(user).also {
            log.info("Usuário '${user.email}' inserido com sucesso.")
        }
    }

    fun findRoleById(roleId: String): Role? {
        log.info("Buscando role com ID '$roleId'.")
        return roleRepository.findByName(roleId)
    }

    fun list(sortDir: SortDir): List<User> {
        log.info("Listando todos os usuários em ordem ${sortDir.name}.")
        return if (sortDir == SortDir.ASC) repository.findAll() else repository.findAll().reversed()
    }

    fun findByIdOrNull(id: Long): User? {
        log.info("Buscando usuário com ID '$id'.")
        return repository.findById(id).orElse(null)
    }

    fun update(id: Long, user: User): User {
        log.info("Atualizando usuário com ID '$id'.")
        val existingUser = repository.findById(id).orElse(null)
            ?: throw NotFoundException("Usuário com ID $id não encontrado!")

        existingUser.name = user.name ?: existingUser.name
        existingUser.email = user.email ?: existingUser.email
        existingUser.password = user.password ?: existingUser.password
        existingUser.roles = user.roles ?: existingUser.roles

        return repository.save(existingUser).also {
            log.info("Usuário com ID '$id' atualizado com sucesso.")
        }
    }

    fun updateEmail(userId: Long, newEmail: String): User {
        log.info("Atualizando email do usuário com ID '$userId' para '$newEmail'.")
        val user = repository.findById(userId).orElseThrow {
            NotFoundException("Usuário com ID $userId não encontrado!")
        }

        if (repository.findByEmail(newEmail) != null) {
            log.warn("O email '$newEmail' já está em uso.")
            throw IllegalArgumentException("O email '$newEmail' já está em uso.")
        }

        user.email = newEmail
        return repository.save(user).also {
            log.info("Email do usuário com ID '$userId' atualizado com sucesso.")
        }
    }

    fun delete(adminId: Long, userId: Long) {
        log.info("Tentativa de exclusão do usuário com ID '$userId' pelo administrador com ID '$adminId'.")
        val admin = repository.findById(adminId).orElseThrow {
            NotFoundException("Administrador com ID $adminId não encontrado!")
        }

        if (!admin.roles.any { it.name == "ADM" }) {
            log.warn("Tentativa de exclusão negada. Apenas administradores podem excluir usuários.")
            throw NotFoundException("Apenas administradores podem excluir usuários!")
        }

        val user = repository.findById(userId).orElseThrow {
            NotFoundException("Usuário com ID $userId não encontrado!")
        }

        repository.deleteById(userId)
        log.info("Usuário com ID '$userId' excluído com sucesso.")
    }

    fun addRoleToUser(userId: Long, roleId: Long): User? {
        log.info("Adicionando role com ID '$roleId' ao usuário com ID '$userId'.")
        val user = repository.findById(userId).orElseThrow {
            NotFoundException("Usuário com ID $userId não encontrado!")
        }

        val role = roleRepository.findById(roleId).orElseThrow {
            NotFoundException("Role com ID $roleId não encontrado!")
        }

        if (!user.roles.contains(role)) {
            user.roles.add(role)
            return repository.save(user).also {
                log.info("Role com ID '$roleId' adicionado ao usuário com ID '$userId' com sucesso.")
            }
        } else {
            log.warn("Usuário com ID '$userId' já possui o role com ID '$roleId'.")
            throw IllegalArgumentException("Usuário já possui este Role!")
        }
    }

    fun findByEmail(email: String): User? {
        log.info("Buscando usuário com email '$email'.")
        return repository.findByEmail(email)
    }

    fun updatePassword(user: User, newPassword: String): Boolean {
        log.info("Atualizando senha do usuário com email '${user.email}'.")
        user.password = newPassword
        repository.save(user)
        log.info("Senha atualizada com sucesso.")
        return true
    }

    @Transactional
    fun updateProfile(email: String, name: String?, profilePic: MultipartFile?): Boolean {
        log.info("Atualizando perfil do usuário com email '$email'.")
        val user = repository.findByEmail(email) ?: return false

        user.name = name ?: user.name

        if (profilePic != null) {
            val fileName = "${profilePic.originalFilename}"
            val filePath = Paths.get("src/main/resources/static/uploads", fileName)
            profilePic.inputStream.use { input ->
                Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING)
            }
            user.profilePic = fileName
        }

        repository.save(user)
        log.info("Perfil do usuário com email '$email' atualizado com sucesso.")
        return true
    }

    fun getAllUsers(): List<User> {
        log.info("Buscando todos os usuários.")
        return repository.findAll().also {
            log.info("Usuários encontrados: $it")
        }
    }

    fun delete(userId: Long): Boolean {
        log.info("Deletando usuário com ID '$userId'.")
        val user = repository.findByIdOrNull(userId) ?: return false
        repository.delete(user)
        log.info("Usuário com ID '$userId' deletado com sucesso.")
        return true
    }

    fun deleteMessagesByUserId(userEmail: String): Boolean {
        log.info("Deletando mensagens do usuário com email '$userEmail'.")
        return mensagensGRepository.deleteByUserId(userEmail).also {
            if (it > 0) log.info("Mensagens do usuário com email '$userEmail' deletadas com sucesso.")
            else log.warn("Nenhuma mensagem encontrada para o usuário com email '$userEmail'.")
        } > 0
    }

    fun findEmailByUserId(userId: Long): String? {
        log.info("Buscando email do usuário com ID '$userId'.")
        return repository.findByIdOrNull(userId)?.email.also {
            log.info("Email encontrado: $it")
        }
    }
}

