package br.pucpr.authserver.roles

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/roles")
class RoleController(
    val service: RoleService
) {
    @PostMapping
    fun insert(
        @RequestBody @Valid role: CreateRoleRequest
    ) = RoleResponse(service.insert(role.toRole()))
        .let { ResponseEntity.status(CREATED).body(it) }

    @GetMapping("/listaroles")
    fun list() =
        service.findAll()
            .map { RoleResponse(it) }
}
