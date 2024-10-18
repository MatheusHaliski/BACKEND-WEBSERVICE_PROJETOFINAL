package br.pucpr.authserver.users

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

enum class SortDir {
    ASC, DESC;

    companion object {
        fun getByName(name: String?): SortDir? {
            val dir = name?.uppercase() ?: "ASC"
            return entries.firstOrNull { it.name == dir }
        }
    }
}
