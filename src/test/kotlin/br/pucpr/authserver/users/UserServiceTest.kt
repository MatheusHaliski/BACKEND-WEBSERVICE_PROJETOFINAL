package br.pucpr.authserver.users

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class UserServiceTest {

    private lateinit var repository: UserRepository
    private lateinit var service: UserService

    @BeforeEach
    fun setUp() {
        repository = mockk()
        service = UserService(repository)
    }

    @Test
    fun `insert throws BadRequestException if the email exists`() {
        // Arrange
        val email = "user@email.com"
        val existingUser = User(email = email, name = "Existing br.pucpr.authserver.users.br.pucpr.authserver.users.User",password="1234") // Preencha os campos necessários
        val newUser = User(email = email, name = "New br.pucpr.authserver.users.br.pucpr.authserver.users.User",password="1234") // Preencha os campos necessários

        every { repository.findByEmail(email) } returns existingUser

        // Act & Assert
        assertFailsWith<BadRequestException> {
            service.insert(newUser)
        }

        verify(exactly = 1) { repository.findByEmail(email) }
        verify(exactly = 0) { repository.save(any()) }
    }
}
