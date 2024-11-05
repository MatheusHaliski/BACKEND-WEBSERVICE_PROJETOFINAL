package br.pucpr.authserver

import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.security.Jwt
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class UserControllerTest {

    private lateinit var userController: UserController
    private lateinit var userService: UserService
    private lateinit var roleRepository: RoleRepository
    private lateinit var jwt: Jwt

    @BeforeEach
    fun setUp() {
        userService = mock(UserService::class.java)
        roleRepository = mock(RoleRepository::class.java)
        jwt = mock(Jwt::class.java)
        userController = UserController(userService, roleRepository, jwt)
    }

    @Test
    fun `test insert user success`() {
        val userRequest = CreateUserRequest(role = "ADMIN")
        val user = User() // Crie um usuário fictício conforme sua implementação

        `when`(roleRepository.findByName(userRequest.role)).thenReturn(Role()) // Mock do papel
        `when`(userService.insert(userRequest)).thenReturn(user)

        val response = userController.insert(userRequest)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `test insert user role not found`() {
        val userRequest = CreateUserRequest(role = "UNKNOWN_ROLE")

        `when`(roleRepository.findByName(userRequest.role)).thenReturn(null)

        val response = userController.insert(userRequest)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue((response.body as Map<*, *>)["message"].toString().contains("Role com ID"))
    }

    @Test
    fun `test list users success`() {
        `when`(userService.list(any())).thenReturn(listOf(User())) // Mock da lista de usuários

        val response = userController.list(null)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `test find user by id success`() {
        val userId = 1L
        val user = User() // Mock do usuário

        `when`(userService.findByIdOrNull(userId)).thenReturn(user)

        val response = userController.findById(userId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `test update user success`() {
        val userId = 1L
        val userRequest = CreateUserRequest(role = "ADMIN")
        val user = User() // Mock do usuário

        `when`(userService.findRoleById(userRequest.role)).thenReturn(Role())
        `when`(userService.update(userId, userRequest.toUser(Role()))).thenReturn(user)

        val response = userController.update(userId, userRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `test login success`() {
        val loginRequest = LoginRequest(email = "test@example.com", password = "password")
        val user = User() // Mock do usuário
        val token = "mockedToken"

        `when`(userService.findByEmailAndPassword(loginRequest.email, loginRequest.password)).thenReturn(user)
        `when`(jwt.createToken(user)).thenReturn(token)

        val response = userController.login(loginRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("mockedToken", (response.body as Map<*, *>)["token"])
    }

    @Test
    fun `test reset password success`() {
        val resetRequest = ResetPasswordRequest(email = "test@example.com", newPassword = "newPassword")
        val user = User() // Mock do usuário

        `when`(userService.findByEmail(resetRequest.email)).thenReturn(user)
        `when`(userService.updatePassword(user, resetRequest.newPassword)).thenReturn(true)

        val response = userController.resetPassword(resetRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Password has been successfully reset", (response.body as Map<*, *>)["message"])
    }
}
