package br.pucpr.authserver.users

import org.springframework.web.multipart.MultipartFile


class UserDTO {
    private val name: String? = null
    private val email: String? = null
    private val password: String? = null

    // MultipartFile para lidar com o upload da imagem
    private val profilePic: MultipartFile? = null // Getters e Setters
}

