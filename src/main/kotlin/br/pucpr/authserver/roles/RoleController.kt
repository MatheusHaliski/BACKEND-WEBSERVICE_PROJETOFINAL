package br.pucpr.authserver.roles

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/roles")
class RoleController(
    val service: RoleService
) {
    @PostMapping
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("hasRole('ADMIN')")
    fun insert(
        @RequestBody @Valid role: CreateRoleRequest
    ) = RoleResponse(service.insert(role.toRole()))
        .let { ResponseEntity.status(CREATED).body(it) }

    @GetMapping
    fun list() =
        service.findAll()
            .map { RoleResponse(it) }
}
